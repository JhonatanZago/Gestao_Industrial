package com.metalurgica.sistemagestaoindustrial.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
public record TurnoRequest(@NotBlank @Size(max = 50) String codigo, @NotBlank @Size(max = 200) String nome, @NotNull LocalTime horaInicio, @NotNull LocalTime horaFim) { }
