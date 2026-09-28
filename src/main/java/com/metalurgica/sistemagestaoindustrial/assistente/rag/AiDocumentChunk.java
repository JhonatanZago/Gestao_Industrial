package com.metalurgica.sistemagestaoindustrial.assistente.rag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ai_document_chunk", indexes = {
        @Index(name = "idx_ai_chunk_document_version", columnList = "document_id,version,active"),
        @Index(name = "idx_ai_chunk_empresa_sector", columnList = "empresa_id,sector_id,active")
})
@Getter
@Setter
@NoArgsConstructor
public class AiDocumentChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "document_id", nullable = false)
    private Long documentId;
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;
    @Column(name = "sector_id")
    private Long sectorId;
    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;
    @Column(nullable = false, length = 80)
    private String version;
    @Column(name = "required_permission", nullable = false, length = 80)
    private String requiredPermission;
    @Column(nullable = false)
    private boolean active;
}
