package com.metalurgica.sistemagestaoindustrial.analytics;

import com.metalurgica.sistemagestaoindustrial.domain.RegistroProducao;

import java.math.BigDecimal;

record AnalyticsRecord(RegistroProducao producao, BigDecimal quantidade, long duracaoBrutaSegundos,
                       long tempoParadoSegundos) {
}
