package com.metalurgica.sistemagestaoindustrial.dto.request;
import jakarta.validation.constraints.*; import java.time.LocalDateTime;
public record ParadaProducaoRequest(@NotNull Long motivoParadaId, @NotNull LocalDateTime inicio, @NotNull LocalDateTime fim, @Size(max=1000) String observacao, Long maquinaId) { }
