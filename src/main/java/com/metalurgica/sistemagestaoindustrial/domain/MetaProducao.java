package com.metalurgica.sistemagestaoindustrial.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "meta_producao", indexes = {
        @Index(name = "idx_meta_empresa_contexto", columnList = "empresa_id,tipo_meta,setor_id,funcionario_id,peca_id,maquina_id,turno_id"),
        @Index(name = "idx_meta_vigencia", columnList = "empresa_id,periodicidade,vigencia_inicio,vigencia_fim,ativo")
})
@Getter @Setter @NoArgsConstructor
public class MetaProducao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @Column(nullable = false, length = 150) private String nome;
    @Column(length = 1000) private String descricao;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_meta", nullable = false, length = 20)
    private TipoMeta tipo;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "setor_id") private Setor setor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "funcionario_id") private Funcionario funcionario;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "peca_id") private Peca peca;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "maquina_id") private Maquina maquina;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "turno_id") private Turno turno;
    @Column(name = "quantidade_meta", nullable = false, precision = 19, scale = 3) private BigDecimal quantidadeMeta;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PeriodicidadeMeta periodicidade;
    @Column(name = "vigencia_inicio", nullable = false) private LocalDate vigenciaInicio;
    @Column(name = "vigencia_fim") private LocalDate vigenciaFim;
    @Column(nullable = false) private boolean ativo = true;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    public void touchForCreate() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; ativo = true; }
    public void touchForUpdate() { updatedAt = OffsetDateTime.now(); }
}
