package com.metalurgica.sistemagestaoindustrial.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "setor", uniqueConstraints = {
        @UniqueConstraint(name = "uk_setor_empresa_codigo", columnNames = {"empresa_id", "codigo"}),
        @UniqueConstraint(name = "uk_setor_empresa_nome", columnNames = {"empresa_id", "nome_normalizado"})
})
@Getter @Setter @NoArgsConstructor
public class Setor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @Column(nullable = false, length = 30)
    private String codigo;
    @Column(nullable = false, length = 200)
    private String nome;
    @Column(name = "nome_normalizado", nullable = false, length = 200)
    private String nomeNormalizado;
    @Column(length = 1000)
    private String descricao;
    @Column(nullable = false)
    private boolean ativo = true;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public void touchForCreate() {
        var now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.ativo = true;
    }

    public void touchForUpdate() { this.updatedAt = OffsetDateTime.now(); }
}
