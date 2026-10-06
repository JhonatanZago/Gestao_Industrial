package com.metalurgica.sistemagestaoindustrial.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "importacao_baseline", uniqueConstraints = @UniqueConstraint(
        name = "uk_import_baseline_source", columnNames = {"empresa_id", "setor_id", "source_key", "nome_arquivo"}))
@Getter @Setter @NoArgsConstructor
public class ImportacaoBaseline {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "empresa_id", nullable = false) private Long empresaId;
    @Column(name = "setor_id", nullable = false) private Long setorId;
    @Column(name = "source_key", nullable = false, length = 64) private String sourceKey;
    @Column(name = "nome_arquivo", nullable = false, length = 255) private String nomeArquivo;
    @Column(name = "hash_arquivo", nullable = false, length = 64) private String hashArquivo;
    @Column(name = "observado_em", nullable = false) private OffsetDateTime observadoEm;
}
