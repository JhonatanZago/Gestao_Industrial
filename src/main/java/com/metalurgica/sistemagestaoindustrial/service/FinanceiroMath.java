package com.metalurgica.sistemagestaoindustrial.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinanceiroMath {
    private FinanceiroMath() { }
    public static BigDecimal valorProduzido(BigDecimal quantidade, BigDecimal unitario) { return quantidade.multiply(unitario); }
    public static BigDecimal porHora(BigDecimal valor, long segundos) { return valor == null || segundos == 0 ? null : valor.divide(BigDecimal.valueOf(segundos), 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(3600)).setScale(2, RoundingMode.HALF_UP); }
    public static BigDecimal variacaoPercentual(BigDecimal atual, BigDecimal anterior) { return anterior.signum() == 0 ? null : atual.subtract(anterior).divide(anterior, 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP); }
}
