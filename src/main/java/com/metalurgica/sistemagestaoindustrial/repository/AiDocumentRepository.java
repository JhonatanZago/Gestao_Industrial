package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.assistente.rag.AiDocument;
import com.metalurgica.sistemagestaoindustrial.assistente.rag.AiDocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiDocumentRepository extends JpaRepository<AiDocument, Long> {
    Optional<AiDocument> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<AiDocument> findFirstByEmpresaIdAndContentHashAndStatus(Long empresaId, String contentHash, AiDocumentStatus status);
    List<AiDocument> findByEmpresaIdAndStatus(Long empresaId, AiDocumentStatus status);
}
