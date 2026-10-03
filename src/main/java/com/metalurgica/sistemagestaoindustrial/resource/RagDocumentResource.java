package com.metalurgica.sistemagestaoindustrial.resource;

import com.metalurgica.sistemagestaoindustrial.assistente.rag.AiDocument;
import com.metalurgica.sistemagestaoindustrial.assistente.rag.RagDocumentService;
import com.metalurgica.sistemagestaoindustrial.assistente.security.AssistenteSecurityContextResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/assistente/documentos")
public class RagDocumentResource {
    private final RagDocumentService service;
    private final AssistenteSecurityContextResolver contextResolver;

    public RagDocumentResource(RagDocumentService service, AssistenteSecurityContextResolver contextResolver) {
        this.service = service;
        this.contextResolver = contextResolver;
    }

    @GetMapping
    public List<DocumentResponse> list(@PathVariable Long empresaId, Authentication authentication) {
        var context = contextResolver.resolve(authentication, empresaId);
        return service.list(context).stream().map(DocumentResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> upload(@PathVariable Long empresaId,
                                                   @RequestParam(required = false) Long setorId,
                                                   @RequestPart("file") MultipartFile file,
                                                   Authentication authentication) throws java.io.IOException {
        var context = contextResolver.resolve(authentication, empresaId);
        var document = service.ingest(context, setorId, file.getOriginalFilename(), file.getContentType(), file.getBytes());
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponse.from(document));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> remove(@PathVariable Long empresaId, @PathVariable Long documentId,
                                       Authentication authentication) {
        service.remove(contextResolver.resolve(authentication, empresaId), documentId);
        return ResponseEntity.noContent().build();
    }

    public record DocumentResponse(Long id, Long empresaId, Long setorId, String fileName,
                                   String contentHash, String version, String status) {
        static DocumentResponse from(AiDocument document) {
            return new DocumentResponse(document.getId(), document.getEmpresaId(), document.getSectorId(),
                    document.getFileName(), document.getContentHash(), document.getVersion(), document.getStatus().name());
        }
    }
}
