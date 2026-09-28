package com.metalurgica.sistemagestaoindustrial.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class ReportAuditLogger {
    private static final Logger LOG = LoggerFactory.getLogger(ReportAuditLogger.class);

    public void generated(Authentication authentication, Long empresaId, String tipo, ReportPeriod period, String formato) {
        var usuarioId = authentication instanceof JwtAuthenticationToken jwt ? jwt.getToken().getSubject() : "anonymous";
        LOG.info("relatorio_gerado usuarioId={} empresaId={} tipoRelatorio={} periodoInicio={} periodoFim={} formato={}",
            usuarioId, empresaId, tipo, period.inicio(), period.fim(), formato);
    }
}
