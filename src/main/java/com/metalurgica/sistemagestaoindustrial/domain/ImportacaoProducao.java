package com.metalurgica.sistemagestaoindustrial.domain;
import jakarta.persistence.*; import lombok.*; import java.time.OffsetDateTime;
@Entity @Table(name="importacao_producao", indexes={@Index(name="idx_import_empresa_created", columnList="empresa_id,created_at"),@Index(name="idx_import_empresa_hash",columnList="empresa_id,setor_id,hash_arquivo"),@Index(name="idx_import_status",columnList="empresa_id,status")}) @Getter @Setter @NoArgsConstructor
public class ImportacaoProducao {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="empresa_id",nullable=false) private Empresa empresa;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="setor_id",nullable=false) private Setor setor;
 @Column(name="nome_arquivo",nullable=false,length=255) private String nomeArquivo;
 @Column(name="hash_arquivo",nullable=false,length=64) private String hashArquivo;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StatusImportacao status;
 @Column(nullable=false) private int totalLinhas; @Column(nullable=false) private int linhasValidas; @Column(nullable=false) private int linhasInvalidas; @Column(nullable=false) private int linhasImportadas;
 @Column(name="iniciado_em",nullable=false) private OffsetDateTime iniciadoEm; @Column(name="finalizado_em") private OffsetDateTime finalizadoEm; @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
 @Column(name="correlation_id",length=100) private String correlationId;
 @Column(name="erro_codigo",length=80) private String erroCodigo;
 @Column(name="erro_mensagem",length=500) private String erroMensagem;
 public void touchCreate(){var n=OffsetDateTime.now();createdAt=n;iniciadoEm=n;status=StatusImportacao.RECEBIDA;}
}
