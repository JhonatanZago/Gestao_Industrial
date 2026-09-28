package com.metalurgica.sistemagestaoindustrial.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

public class RequestAuthorizationFilter extends OncePerRequestFilter {
    private static final Pattern COMPANY_PATH = Pattern.compile("^/api/v1/empresas/(\\d+)(?:/.*)?$");
    private final AuthorizationService authorizationService;
    private final SectorScopeService sectorScopeService;

    public RequestAuthorizationFilter(AuthorizationService authorizationService, SectorScopeService sectorScopeService) {
        this.authorizationService = authorizationService;
        this.sectorScopeService = sectorScopeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        var path = request.getRequestURI();
        if (path.startsWith("/api/v1/auth/") || path.equals("/actuator/health")) {
            chain.doFilter(request, response);
            return;
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            chain.doFilter(request, response);
            return;
        }
        if (path.equals("/api/v1/empresas") && "POST".equalsIgnoreCase(request.getMethod())) {
            if (authorizationService.hasPermission(authentication, "EMPRESA_GERENCIAR")) {
                chain.doFilter(request, response);
            } else {
                deny(response);
            }
            return;
        }
        var matcher = COMPANY_PATH.matcher(path);
        if (!matcher.matches() || !authorizationService.belongsToCompany(authentication, Long.valueOf(matcher.group(1)))) {
            deny(response); return;
        }
        var companyId = Long.valueOf(matcher.group(1));
        var jwt = (JwtAuthenticationToken) authentication;
        var sectorId = explicitSectorId(request);
        if (sectorId != null) {
            var userId = Long.valueOf(jwt.getToken().getSubject());
            var profile = jwt.getToken().getClaimAsString("perfil");
            if (!sectorScopeService.canAccess(userId, profile, companyId, sectorId)) {
                deny(response); return;
            }
        }
        var required = requiredPermission(path, request.getMethod());
        if (required == null || !authorizationService.hasPermission(authentication, required)) {
            deny(response); return;
        }
        chain.doFilter(request, response);
    }

    private String requiredPermission(String path, String method) {
        boolean write = !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method);
        if (path.contains("/assistente/documentos")) return "AI_DOCUMENTO_GERENCIAR";
        if (path.contains("/assistente")) return "ANALYTICS_VISUALIZAR";
        if (path.contains("/usuarios")) return write ? "USUARIO_GERENCIAR" : "USUARIO_VISUALIZAR";
        if (path.contains("/setores")) return write ? "SETOR_GERENCIAR" : "SETOR_VISUALIZAR";
        if (path.contains("/funcionarios")) return write ? "FUNCIONARIO_GERENCIAR" : "FUNCIONARIO_VISUALIZAR";
        if (path.contains("/pecas")) return write ? "PECA_GERENCIAR" : "PECA_VISUALIZAR";
        if (path.contains("/maquinas")) return write ? "MAQUINA_GERENCIAR" : "MAQUINA_VISUALIZAR";
        if (path.contains("/turnos")) return write ? "TURNO_GERENCIAR" : "TURNO_VISUALIZAR";
        if (path.contains("/importacoes/producao") && path.endsWith("/qualidade")) return "QUALIDADE_DADOS_VISUALIZAR";
        if (path.contains("/importacoes/producao")) return write ? "PRODUCAO_IMPORTAR" : "PRODUCAO_VISUALIZAR";
        if (path.contains("/producoes")) return write ? "PRODUCAO_REGISTRAR" : "PRODUCAO_VISUALIZAR";
        if (path.contains("/paradas")) return write ? "PARADA_GERENCIAR" : "PARADA_VISUALIZAR";
        if (path.contains("/metas-producao") || path.contains("/analytics/metas")) return write ? "META_GERENCIAR" : "META_VISUALIZAR";
        if (path.contains("/tempos-padrao")) return write ? "TEMPO_PADRAO_GERENCIAR" : "TEMPO_PADRAO_VISUALIZAR";
        if (path.contains("/valores-producao")) return write ? "VALOR_PRODUCAO_GERENCIAR" : "FINANCEIRO_INDUSTRIAL_VISUALIZAR";
        if (path.contains("/analytics/financeiro")) return "FINANCEIRO_INDUSTRIAL_VISUALIZAR";
        if (path.contains("/analytics/desempenho")) return "DESEMPENHO_VISUALIZAR";
        if (path.contains("/analytics")) return "ANALYTICS_VISUALIZAR";
        if (path.contains("/relatorios/financeiro-industrial")) return "FINANCEIRO_INDUSTRIAL_VISUALIZAR";
        if (path.contains("/relatorios/")) return "RELATORIO_VISUALIZAR";
        if (path.contains("/auditoria")) return "AUDITORIA_VISUALIZAR";
        if (path.contains("/qualidade-dados")) return "QUALIDADE_DADOS_VISUALIZAR";
        if (path.contains("/motivos-parada")) return write ? "PARADA_GERENCIAR" : "PARADA_VISUALIZAR";
        if (path.equals("/api/v1/empresas") || path.matches("^/api/v1/empresas/\\d+(?:/(?:ativar|desativar))?$")) {
            return write ? "EMPRESA_GERENCIAR" : "EMPRESA_VISUALIZAR";
        }
        return null;
    }

    private void deny(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"erro\":\"Acesso negado\"}");
    }

    private Long explicitSectorId(HttpServletRequest request) {
        var query = request.getParameter("setorId");
        if (query != null && !query.isBlank()) {
            try { return Long.valueOf(query); } catch (NumberFormatException ignored) { return -1L; }
        }
        var path = request.getRequestURI();
        var marker = "/setores/";
        var index = path.indexOf(marker);
        if (index < 0) return null;
        var start = index + marker.length();
        var end = path.indexOf('/', start);
        var value = end < 0 ? path.substring(start) : path.substring(start, end);
        try { return Long.valueOf(value); } catch (NumberFormatException ignored) { return -1L; }
    }
}
