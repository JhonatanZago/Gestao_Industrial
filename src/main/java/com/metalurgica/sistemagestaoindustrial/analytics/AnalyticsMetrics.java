package com.metalurgica.sistemagestaoindustrial.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record AnalyticsMetrics(BigDecimal quantidadeProduzida, long quantidadeRegistros,
                              BigDecimal duracaoBrutaMinutos, BigDecimal tempoParadoMinutos,
                              BigDecimal tempoProdutivoMinutos, BigDecimal taxaProducaoBruta,
                              BigDecimal taxaProducaoProdutiva, BigDecimal percentualTempoParado) {
    private static final int SCALE = 2;
    private static final BigDecimal SIXTY = BigDecimal.valueOf(60);

    public static AnalyticsMetrics empty() {
        return from(BigDecimal.ZERO, 0, 0, 0);
    }

    public static AnalyticsMetrics from(BigDecimal quantity, long records, long grossSeconds, long stoppedSeconds) {
        long productiveSeconds = Math.max(0, grossSeconds - stoppedSeconds);
        BigDecimal grossMinutesRaw = rawMinutes(grossSeconds);
        BigDecimal stoppedMinutesRaw = rawMinutes(stoppedSeconds);
        BigDecimal productiveMinutesRaw = rawMinutes(productiveSeconds);
        return new AnalyticsMetrics(
                quantity == null ? BigDecimal.ZERO : quantity,
                records,
                display(grossMinutesRaw),
                display(stoppedMinutesRaw),
                display(productiveMinutesRaw),
                rate(quantity, grossMinutesRaw),
                rate(quantity, productiveMinutesRaw),
                percentage(stoppedMinutesRaw, grossMinutesRaw));
    }

    private static BigDecimal rawMinutes(long seconds) {
        return BigDecimal.valueOf(seconds).divide(SIXTY, 8, RoundingMode.HALF_UP);
    }

    private static BigDecimal display(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal rate(BigDecimal quantity, BigDecimal minutes) {
        if (quantity == null || minutes == null || minutes.signum() == 0) return null;
        return quantity.divide(minutes.divide(SIXTY, 8, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal percentage(BigDecimal part, BigDecimal total) {
        if (part == null || total == null || total.signum() == 0) return null;
        return part.divide(total, 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
