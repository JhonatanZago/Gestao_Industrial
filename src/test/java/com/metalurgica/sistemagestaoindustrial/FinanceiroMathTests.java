package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.service.FinanceiroMath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FinanceiroMathTests {
    @Test void calculaValorComPrecisaoDecimal() {
        assertEquals(0, FinanceiroMath.valorProduzido(new BigDecimal("350"), new BigDecimal("0.85")).compareTo(new BigDecimal("297.50")));
        assertEquals(0, FinanceiroMath.valorProduzido(new BigDecimal("1000"), new BigDecimal("0.0085")).compareTo(new BigDecimal("8.50")));
    }

    @Test void calculaValorPorHoraETrataZero() {
        assertEquals(new BigDecimal("250.00"), FinanceiroMath.porHora(new BigDecimal("500"), 7200));
        assertNull(FinanceiroMath.porHora(new BigDecimal("500"), 0));
    }

    @Test void calculaVariacaoSemDividirPorZero() {
        assertEquals(new BigDecimal("4.00"), FinanceiroMath.variacaoPercentual(new BigDecimal("104"), new BigDecimal("100")));
        assertNull(FinanceiroMath.variacaoPercentual(new BigDecimal("104"), BigDecimal.ZERO));
    }
}
