package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.dto.response.*;
import com.metalurgica.sistemagestaoindustrial.quality.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QualidadeDadosContractsTests {
    @Test
    void possuiEnumsEContratosEstaveis() {
        assertArrayEquals(new String[]{"INFO", "ALERTA", "CRITICO"},
                java.util.Arrays.stream(SeveridadeQualidadeDados.values()).map(Enum::name).toArray(String[]::new));
        assertTrue(java.util.Arrays.stream(CategoriaQualidadeDados.values()).anyMatch(v -> v.name().equals("DUPLICIDADE")));
        var problema = new ProblemaQualidadeResponse("QD-PROD-001", CategoriaQualidadeDados.COBERTURA,
                SeveridadeQualidadeDados.ALERTA, "Sem padrão", 7L,
                TipoRecursoQualidadeDados.REGISTRO_PRODUCAO, 10L, 3L,
                LocalDateTime.of(2026, 5, 15, 8, 0), Map.of("percentual", new BigDecimal("80.00")));
        assertEquals("QD-PROD-001", problema.codigo());
        assertEquals(LocalDateTime.of(2026, 5, 15, 8, 0), problema.dataReferencia());
        assertEquals(new BigDecimal("80.00"), problema.detalhes().get("percentual"));
    }

    @Test
    void preservaNulosEListaDeRegistrosDeDuplicidade() {
        var response = new DuplicidadeProducaoResponse("QD-DUP-001", 7L, 3L, 1L, 2L,
                null, null, LocalDateTime.of(2026, 5, 15, 8, 0),
                LocalDateTime.of(2026, 5, 15, 10, 0), new BigDecimal("300.000"), List.of(10L, 11L));
        assertEquals("QD-DUP-001", response.grupoDuplicidade());
        assertEquals(List.of(10L, 11L), response.registrosRelacionados());
        assertNull(response.maquinaId());
        assertEquals(new BigDecimal("300.000"), response.quantidade());
    }

    @Test
    void centralizaLimitesConfiguraveis() {
        var properties = new QualidadeDadosProperties();
        assertEquals(24, properties.getMaxProductionDuration().toHours());
        assertEquals(100, properties.getMaxPageSize());
        assertEquals(10_000, properties.getMaxRecords());
        assertEquals(0, properties.getDuplicateTolerance().toSeconds());
        assertEquals(0, new BigDecimal("80").compareTo(properties.getCoverageAlertThresholdPercent()));
    }
}
