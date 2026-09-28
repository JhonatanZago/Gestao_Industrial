package com.metalurgica.sistemagestaoindustrial.repository;

import com.metalurgica.sistemagestaoindustrial.assistente.rag.AiDocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiDocumentChunkRepository extends JpaRepository<AiDocumentChunk, Long> {
    List<AiDocumentChunk> findByDocumentIdAndVersionAndActiveTrue(Long documentId, String version);
    List<AiDocumentChunk> findByDocumentIdAndActiveTrue(Long documentId);
}
