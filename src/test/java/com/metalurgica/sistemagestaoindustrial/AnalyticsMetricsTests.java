package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.analytics.AnalyticsMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AnalyticsMetricsTests {
    @Test
    void calculaCenarioDeterministico() {
        var metrics = AnalyticsMetrics.from(new BigDecimal("500"), 2, 14_400, 1_800);

        assertEquals(new BigDecimal("500"), metrics.quantidadeProduzida());
        assertEquals(2, metrics.quantidadeRegistros());
        assertEquals(new BigDecimal("240.00"), metrics.duracaoBrutaMinutos());
        assertEquals(new BigDecimal("30.00"), metrics.tempoParadoMinutos());
        assertEquals(new BigDecimal("210.00"), metrics.tempoProdutivoMinutos());
        assertEquals(new BigDecimal("125.00"), metrics.taxaProducaoBruta());
        assertEquals(new BigDecimal("142.86"), metrics.taxaProducaoProdutiva());
        assertEquals(new BigDecimal("12.50"), metrics.percentualTempoParado());
    }

    @Test
    void retornaNuloQuandoDenominadorEZero() {
        var metrics = AnalyticsMetrics.from(new BigDecimal("10"), 1, 0, 0);

        assertNull(metrics.taxaProducaoBruta());
        assertNull(metrics.taxaProducaoProdutiva());
        assertNull(metrics.percentualTempoParado());
    }
}
