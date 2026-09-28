package com.metalurgica.sistemagestaoindustrial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.metalurgica.sistemagestaoindustrial.quality.StatusConfiabilidadeDados;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record QualidadeDadosResumoResponse(
        Long empresaId,
        Long setorId,
        LocalDateTime inicio,
        LocalDateTime fim,
        CoberturaDadosResponse coberturaTempoPadrao,
        CoberturaDadosResponse coberturaValor,
        Map<String, CoberturaDadosResponse> coberturaValorPorMoeda,
        long alertas,
        long alertasCriticos,
        long duplicidades,
        StatusConfiabilidadeDados status) {
    public QualidadeDadosResumoResponse(Long empresaId, Long setorId, LocalDateTime inicio, LocalDateTime fim,
                                        CoberturaDadosResponse coberturaTempoPadrao, CoberturaDadosResponse coberturaValor,
                                        long alertas, long alertasCriticos, StatusConfiabilidadeDados status) {
        this(empresaId, setorId, inicio, fim, coberturaTempoPadrao, coberturaValor, Map.of(), alertas, alertasCriticos, 0, status);
    }

    public QualidadeDadosResumoResponse(Long empresaId, Long setorId, LocalDateTime inicio, LocalDateTime fim,
                                        CoberturaDadosResponse coberturaTempoPadrao, CoberturaDadosResponse coberturaValor,
                                        long alertas, long alertasCriticos, long duplicidades, StatusConfiabilidadeDados status) {
        this(empresaId, setorId, inicio, fim, coberturaTempoPadrao, coberturaValor, Map.of(), alertas, alertasCriticos, duplicidades, status);
    }
}
