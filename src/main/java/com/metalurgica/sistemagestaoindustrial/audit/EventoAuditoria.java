package com.metalurgica.sistemagestaoindustrial.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "evento_auditoria", indexes = {
        @Index(name = "idx_evento_auditoria_empresa_data", columnList = "empresa_id,data_hora"),
        @Index(name = "idx_evento_auditoria_empresa_recurso", columnList = "empresa_id,tipo_recurso,recurso_id"),
        @Index(name = "idx_evento_auditoria_empresa_usuario_data", columnList = "empresa_id,usuario_id,data_hora"),
        @Index(name = "idx_evento_auditoria_empresa_acao_data", columnList = "empresa_id,acao,data_hora"),
        @Index(name = "idx_evento_auditoria_correlation", columnList = "correlation_id")
})
@Getter
@NoArgsConstructor
public class EventoAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_nome_snapshot", length = 200)
    private String usuarioNomeSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso", nullable = false, length = 40)
    private TipoRecursoAuditoria tipoRecurso;

    @Column(name = "recurso_id")
    private Long recursoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AcaoAuditoria acao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrigemAuditoria origem;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    @Column(name = "dados_anteriores", columnDefinition = "TEXT")
    private String dadosAnteriores;

    @Column(name = "dados_novos", columnDefinition = "TEXT")
    private String dadosNovos;

    @Column(columnDefinition = "TEXT")
    private String metadados;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    public EventoAuditoria(Long empresaId, Long usuarioId, String usuarioNomeSnapshot,
                           TipoRecursoAuditoria tipoRecurso, Long recursoId, AcaoAuditoria acao,
                           OrigemAuditoria origem, OffsetDateTime dataHora,
                           String dadosAnteriores, String dadosNovos, String metadados,
                           String correlationId) {
        this.empresaId = empresaId;
        this.usuarioId = usuarioId;
        this.usuarioNomeSnapshot = usuarioNomeSnapshot;
        this.tipoRecurso = tipoRecurso;
        this.recursoId = recursoId;
        this.acao = acao;
        this.origem = origem;
        this.dataHora = dataHora;
        this.dadosAnteriores = dadosAnteriores;
        this.dadosNovos = dadosNovos;
        this.metadados = metadados;
        this.correlationId = correlationId;
    }
}
