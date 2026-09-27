package com.metalurgica.sistemagestaoindustrial.domain;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="resultado_linha_importacao", indexes=@Index(name="idx_resultado_import_linha",columnList="importacao_id,numero_linha")) @Getter @Setter @NoArgsConstructor
public class ResultadoLinhaImportacao {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="importacao_id",nullable=false) private ImportacaoProducao importacao;
 @Column(name="numero_linha",nullable=false) private int numeroLinha; @Column(nullable=false,length=20) private String status; @Column(name="codigo_funcionario",length=100) private String codigoFuncionario; @Column(name="codigo_peca",length=100) private String codigoPeca; @Column(name="codigo_erro",length=80) private String codigoErro; @Column(length=1000) private String mensagem;
}
