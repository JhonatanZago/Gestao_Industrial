package com.metalurgica.sistemagestaoindustrial.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "valor_producao", indexes = {
        @Index(name = "idx_valor_producao_empresa_contexto", columnList = "empresa_id,peca_id,setor_id,maquina_id"),
        @Index(name = "idx_valor_producao_vigencia", columnList = "empresa_id,tipo_valor,moeda,vigencia_inicio,vigencia_fim,ativo")
})
@Getter @Setter @NoArgsConstructor
public class ValorProducao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "empresa_id", nullable = false) private Empresa empresa;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "peca_id", nullable = false) private Peca peca;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "setor_id", nullable = false) private Setor setor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "maquina_id") private Maquina maquina;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_valor", nullable = false, length = 24) private TipoValor tipoValor;
    @Column(name = "valor_unitario", nullable = false, precision = 19, scale = 6) private BigDecimal valorUnitario;
    @Column(nullable = false, length = 3) private String moeda;
    @Column(name = "vigencia_inicio", nullable = false) private LocalDate vigenciaInicio;
    @Column(name = "vigencia_fim") private LocalDate vigenciaFim;
    @Column(nullable = false) private boolean ativo = true;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    public void touchForCreate() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; ativo = true; }
    public void touchForUpdate() { updatedAt = OffsetDateTime.now(); }
}
