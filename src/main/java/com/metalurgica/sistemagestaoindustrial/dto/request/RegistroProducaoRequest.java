package com.metalurgica.sistemagestaoindustrial.dto.request;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.LocalDateTime;
public record RegistroProducaoRequest(@NotNull Long setorId, @NotNull Long funcionarioId, @NotNull Long pecaId, @NotNull @DecimalMin(value="0.0", inclusive=false) @Digits(integer=16, fraction=3) BigDecimal quantidade, @NotNull LocalDateTime inicio, @NotNull LocalDateTime fim, @Size(max=1000) String observacao, Long maquinaId, Long turnoId) { }
