package com.metalurgica.sistemagestaoindustrial.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "maquina", uniqueConstraints = @UniqueConstraint(name = "uk_maquina_empresa_codigo", columnNames = {"empresa_id", "codigo"}), indexes = {
        @Index(name = "idx_maquina_empresa_setor", columnList = "empresa_id,setor_id"),
        @Index(name = "idx_maquina_empresa_ativo", columnList = "empresa_id,ativo")
})
@Getter @Setter @NoArgsConstructor
public class Maquina {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "empresa_id", nullable = false) private Empresa empresa;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "setor_id", nullable = false) private Setor setor;
    @Column(nullable = false, length = 50) private String codigo;
    @Column(nullable = false, length = 200) private String nome;
    @Column(length = 1000) private String descricao;
    @Column(length = 50) private String tipo;
    @Column(nullable = false) private boolean ativo = true;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    public void touchForCreate() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; ativo = true; }
    public void touchForUpdate() { updatedAt = OffsetDateTime.now(); }
}
