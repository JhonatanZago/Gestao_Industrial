package com.metalurgica.sistemagestaoindustrial.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "registro_producao", indexes = {
        @Index(name = "idx_producao_empresa_inicio", columnList = "empresa_id,inicio"),
        @Index(name = "idx_producao_funcionario_periodo", columnList = "funcionario_id,inicio,fim"),
        @Index(name = "idx_producao_setor_inicio", columnList = "setor_id,inicio"),
        @Index(name = "idx_producao_peca_inicio", columnList = "peca_id,inicio")
})
@Getter @Setter @NoArgsConstructor
public class RegistroProducao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "peca_id", nullable = false)
    private Peca peca;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "maquina_id")
    private Maquina maquina;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "turno_id")
    private Turno turno;
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidade;
    @Column(nullable = false)
    private LocalDateTime inicio;
    @Column(nullable = false)
    private LocalDateTime fim;
    @Column(length = 1000)
    private String observacao;
    @Column(nullable = false)
    private boolean ativo = true;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public void touchForCreate() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; ativo = true; }
    public void touchForUpdate() { updatedAt = OffsetDateTime.now(); }
}
