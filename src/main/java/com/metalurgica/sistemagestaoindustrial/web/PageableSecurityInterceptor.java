package com.metalurgica.sistemagestaoindustrial.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

@Component
public class PageableSecurityInterceptor implements HandlerInterceptor, WebMvcConfigurer {
    private static final Set<String> ALLOWED_FIELDS = Set.of(
        "id", "codigo", "nome", "descricao", "email", "status", "ativo", "categoria", "tipo",
        "inicio", "fim", "data", "dataHora", "vigenteEm", "createdAt", "updatedAt", "quantidade",
        "empresaId", "setorId", "funcionarioId", "pecaId", "maquinaId", "turnoId", "importacaoId"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        validatePageParameter(request, "page", 0, Integer.MAX_VALUE);
        validatePageParameter(request, "size", 1, 100);
        var values = request.getParameterValues("sort");
        if (values == null) return true;
        for (var value : values) {
            var parts = value.split(",", -1);
            if (parts.length < 1 || parts.length > 2 || !ALLOWED_FIELDS.contains(parts[0])
                    || (parts.length == 2 && !Set.of("asc", "desc").contains(parts[1].toLowerCase(java.util.Locale.ROOT)))) {
                throw new IllegalArgumentException("Ordenação inválida.");
            }
        }
        return true;
    }

    private void validatePageParameter(HttpServletRequest request, String name, int minimum, int maximum) {
        var value = request.getParameter(name);
        if (value == null) return;
        try {
            var parsed = Integer.parseInt(value);
            if (parsed < minimum || parsed > maximum) throw new IllegalArgumentException("Parâmetro de paginação inválido.");
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Parâmetro de paginação inválido.");
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/api/**");
    }
}
