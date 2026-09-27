package com.metalurgica.sistemagestaoindustrial.dto.response;
import java.time.*;
public record TurnoResponse(Long id, Long empresaId, String codigo, String nome, LocalTime horaInicio, LocalTime horaFim, long duracaoNominalMinutos, boolean ativo, OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
