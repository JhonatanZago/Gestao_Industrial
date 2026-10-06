package com.metalurgica.sistemagestaoindustrial.importacao;

import java.time.OffsetDateTime;

public record ReconciliationResult(OffsetDateTime iniciadoEm, OffsetDateTime finalizadoEm,
                                   int arquivosEncontrados, int arquivosVerificados,
                                   int arquivosIgnorados, int arquivosProcessados, int arquivosComErro) { }
