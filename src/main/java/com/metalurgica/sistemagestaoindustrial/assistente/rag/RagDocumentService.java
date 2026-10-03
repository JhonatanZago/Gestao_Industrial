package com.metalurgica.sistemagestaoindustrial.assistente.rag;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.EmbeddingGateway;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.VectorStorePort;
import com.metalurgica.sistemagestaoindustrial.assistente.security.AssistenteSecurityContext;
import com.metalurgica.sistemagestaoindustrial.assistente.tool.AiToolException;
import com.metalurgica.sistemagestaoindustrial.audit.AcaoAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.AuditoriaService;
import com.metalurgica.sistemagestaoindustrial.audit.OrigemAuditoria;
import com.metalurgica.sistemagestaoindustrial.audit.TipoRecursoAuditoria;
import com.metalurgica.sistemagestaoindustrial.repository.AiDocumentChunkRepository;
import com.metalurgica.sistemagestaoindustrial.repository.AiDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class RagDocumentService {
    private static final String DOCUMENT_PERMISSION = "AI_DOCUMENTO_GERENCIAR";
    private final AiDocumentRepository documentRepository;
    private final AiDocumentChunkRepository chunkRepository;
    private final DocumentContentValidator validator;
    private final DeterministicChunker chunker;
    private final EmbeddingGateway embeddingGateway;
    private final VectorStorePort vectorStore;
    private final AuditoriaService auditoriaService;

    public RagDocumentService(AiDocumentRepository documentRepository, AiDocumentChunkRepository chunkRepository,
                              DocumentContentValidator validator, DeterministicChunker chunker,
                              EmbeddingGateway embeddingGateway, VectorStorePort vectorStore,
                              AuditoriaService auditoriaService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.validator = validator;
        this.chunker = chunker;
        this.embeddingGateway = embeddingGateway;
        this.vectorStore = vectorStore;
        this.auditoriaService = auditoriaService;
    }

    public AiDocument ingest(AssistenteSecurityContext context, String fileName, String contentType, byte[] bytes) {
        return ingest(context, null, fileName, contentType, bytes);
    }

    public AiDocument ingest(AssistenteSecurityContext context, Long sectorId, String fileName, String contentType, byte[] bytes) {
        requireDocumentPermission(context);
        if (sectorId != null && !context.canAccessSector(sectorId)) {
            throw new AiToolException(AssistenteErrorCode.AI_PERMISSION_DENIED, "Setor do documento fora do escopo autorizado.");
        }
        var normalized = validator.validateAndNormalize(fileName, contentType, bytes);
        var hash = sha256(normalized);
        var existing = documentRepository.findFirstByEmpresaIdAndContentHashAndStatus(context.empresaId(), hash, AiDocumentStatus.ACTIVE);
        if (existing.isPresent()) return existing.get();
        var document = new AiDocument();
        document.setEmpresaId(context.empresaId());
        document.setSectorId(sectorId);
        document.setFileName(fileName);
        document.setTitle(fileName);
        document.setContentType(contentType == null ? "text/plain" : contentType);
        document.setContentHash(hash);
        document.setVersion(hash);
        document.setStatus(AiDocumentStatus.INDEXING);
        document.setRequiredPermission(DOCUMENT_PERMISSION);
        document.setNormalizedContent(normalized);
        document.setCreatedAt(OffsetDateTime.now());
        document.setUpdatedAt(OffsetDateTime.now());
        document = documentRepository.save(document);
        var chunks = chunker.chunk(normalized);
        var previous = documentRepository.findByEmpresaIdAndStatus(context.empresaId(), AiDocumentStatus.ACTIVE).stream()
                .filter(candidate -> candidate.getFileName().equalsIgnoreCase(fileName)).findFirst().orElse(null);
        try {
            var entities = new java.util.ArrayList<AiDocumentChunk>();
            for (int index = 0; index < chunks.size(); index++) {
                var text = chunks.get(index);
                var chunkHash = sha256(text);
                var embedding = embeddingGateway.embed(text);
                var chunk = new AiDocumentChunk();
                chunk.setDocumentId(document.getId());
                chunk.setEmpresaId(context.empresaId());
                chunk.setSectorId(sectorId);
                chunk.setChunkIndex(index);
                chunk.setContent(text);
                chunk.setContentHash(chunkHash);
                chunk.setVersion(document.getVersion());
                chunk.setRequiredPermission(DOCUMENT_PERMISSION);
                chunk.setActive(true);
                entities.add(chunk);
                var metadata = new java.util.LinkedHashMap<String, Object>();
                metadata.put("empresaId", context.empresaId());
                metadata.put("documentId", document.getId());
                metadata.put("permission", DOCUMENT_PERMISSION);
                metadata.put("status", AiDocumentStatus.ACTIVE.name());
                if (sectorId != null) metadata.put("sectorId", sectorId);
                vectorStore.upsert(new VectorStorePort.VectorDocument(
                        document.getId() + ":" + index, document.getId().toString(), document.getVersion(),
                        embedding.vector(), text, metadata));
            }
            chunkRepository.saveAll(entities);
            document.setStatus(AiDocumentStatus.ACTIVE);
            document.setUpdatedAt(OffsetDateTime.now());
            var saved = documentRepository.save(document);
            if (previous != null) deactivate(previous);
            audit(saved, AcaoAuditoria.IMPORTACAO);
            return saved;
        } catch (RuntimeException exception) {
            vectorStore.delete(document.getId().toString(), document.getVersion());
            document.setStatus(AiDocumentStatus.FAILED);
            document.setUpdatedAt(OffsetDateTime.now());
            documentRepository.save(document);
            throw exception;
        }
    }

    @Transactional
    public void remove(AssistenteSecurityContext context, Long documentId) {
        requireDocumentPermission(context);
        var document = documentRepository.findByIdAndEmpresaId(documentId, context.empresaId())
                .orElseThrow(() -> new AiToolException(AssistenteErrorCode.AI_DOCUMENT_INVALID, "Documento não encontrado."));
        chunkRepository.findByDocumentIdAndActiveTrue(documentId).forEach(chunk -> {
            chunk.setActive(false);
            vectorStore.delete(documentId.toString(), chunk.getVersion());
        });
        document.setStatus(AiDocumentStatus.DELETED);
        document.setUpdatedAt(OffsetDateTime.now());
        documentRepository.save(document);
        audit(document, AcaoAuditoria.DESATIVACAO);
    }

    public List<VectorStorePort.VectorMatch> search(AssistenteSecurityContext context, String query, int limit) {
        if (query == null || query.isBlank()) return List.of();
        var vector = embeddingGateway.embed(query).vector();
        var filters = new java.util.LinkedHashMap<String, Object>();
        filters.put("empresaId", context.empresaId());
        filters.put("permission", DOCUMENT_PERMISSION);
        filters.put("status", AiDocumentStatus.ACTIVE.name());
        if (context.allSectors()) return vectorStore.search(vector, filters, Math.max(1, limit));
        var matches = new java.util.ArrayList<VectorStorePort.VectorMatch>();
        for (var sectorId : context.sectorIds()) {
            var scoped = new java.util.LinkedHashMap<>(filters);
            scoped.put("sectorId", sectorId);
            matches.addAll(vectorStore.search(vector, scoped, Math.max(1, limit)));
        }
        return matches.stream().limit(Math.max(1, limit)).toList();
    }

    public List<AiDocument> list(AssistenteSecurityContext context) {
        requireDocumentPermission(context);
        return documentRepository.findByEmpresaIdAndStatus(context.empresaId(), AiDocumentStatus.ACTIVE).stream()
                .filter(document -> document.getSectorId() == null || context.canAccessSector(document.getSectorId()))
                .toList();
    }

    private void deactivate(AiDocument previous) {
        chunkRepository.findByDocumentIdAndActiveTrue(previous.getId()).forEach(chunk -> {
            chunk.setActive(false);
            vectorStore.delete(previous.getId().toString(), chunk.getVersion());
        });
        previous.setStatus(AiDocumentStatus.DELETED);
        previous.setUpdatedAt(OffsetDateTime.now());
        documentRepository.save(previous);
    }

    private void audit(AiDocument document, AcaoAuditoria action) {
        auditoriaService.record(document.getEmpresaId(), TipoRecursoAuditoria.DOCUMENTO, document.getId(), action,
                OrigemAuditoria.API, null, null, Map.of("documentId", document.getId(), "contentHash", document.getContentHash()),
                "ai-document-" + document.getId());
    }

    private void requireDocumentPermission(AssistenteSecurityContext context) {
        if (!context.hasPermission(DOCUMENT_PERMISSION)) {
            throw new AiToolException(AssistenteErrorCode.AI_PERMISSION_DENIED, "Permissão de documentos insuficiente.");
        }
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível calcular hash do documento.", exception);
        }
    }
}
