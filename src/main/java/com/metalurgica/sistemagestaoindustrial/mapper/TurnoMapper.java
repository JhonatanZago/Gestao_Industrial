package com.metalurgica.sistemagestaoindustrial.mapper;
import com.metalurgica.sistemagestaoindustrial.domain.Turno;
import com.metalurgica.sistemagestaoindustrial.dto.response.TurnoResponse;
import org.springframework.stereotype.Component;
import java.time.Duration;
@Component public class TurnoMapper { public TurnoResponse toResponse(Turno t) { long minutes = Duration.between(t.getHoraInicio(), t.getHoraFim()).toMinutes(); if (minutes <= 0) minutes += 24 * 60; return new TurnoResponse(t.getId(), t.getEmpresa().getId(), t.getCodigo(), t.getNome(), t.getHoraInicio(), t.getHoraFim(), minutes, t.isAtivo(), t.getCreatedAt(), t.getUpdatedAt()); } }
