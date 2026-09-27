package com.metalurgica.sistemagestaoindustrial.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "turno", uniqueConstraints = @UniqueConstraint(name = "uk_turno_empresa_codigo", columnNames = {"empresa_id", "codigo"}), indexes = {
        @Index(name = "idx_turno_empresa_ativo", columnList = "empresa_id,ativo")
})
@Getter @Setter @NoArgsConstructor
public class Turno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "empresa_id", nullable = false) private Empresa empresa;
    @Column(nullable = false, length = 50) private String codigo;
    @Column(nullable = false, length = 200) private String nome;
    @Column(name = "hora_inicio", nullable = false) private LocalTime horaInicio;
    @Column(name = "hora_fim", nullable = false) private LocalTime horaFim;
    @Column(nullable = false) private boolean ativo = true;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    public void touchForCreate() { var now = OffsetDateTime.now(); createdAt = now; updatedAt = now; ativo = true; }
    public void touchForUpdate() { updatedAt = OffsetDateTime.now(); }
}
