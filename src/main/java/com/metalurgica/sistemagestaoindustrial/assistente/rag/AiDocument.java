package com.metalurgica.sistemagestaoindustrial.assistente.rag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_document", indexes = {
        @Index(name = "idx_ai_document_empresa_status", columnList = "empresa_id,status"),
        @Index(name = "idx_ai_document_empresa_hash", columnList = "empresa_id,content_hash")
})
@Getter
@Setter
@NoArgsConstructor
public class AiDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;
    @Column(name = "sector_id")
    private Long sectorId;
    @Column(nullable = false, length = 255)
    private String fileName;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 20)
    private String contentType;
    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;
    @Column(nullable = false, length = 80)
    private String version;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiDocumentStatus status;
    @Column(name = "required_permission", nullable = false, length = 80)
    private String requiredPermission;
    @Column(name = "normalized_content", nullable = false, columnDefinition = "TEXT")
    private String normalizedContent;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
