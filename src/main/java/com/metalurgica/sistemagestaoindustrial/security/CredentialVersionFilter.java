package com.metalurgica.sistemagestaoindustrial.security;

import com.metalurgica.sistemagestaoindustrial.domain.StatusUsuario;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CredentialVersionFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarioRepository;

    public CredentialVersionFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            var jwt = jwtAuthentication.getToken();
            var userId = Long.valueOf(jwt.getSubject());
            var versionClaim = jwt.getClaim("credenciaisVersao");
            var valid = versionClaim instanceof Number number
                && usuarioRepository.findById(userId)
                    .filter(user -> user.getStatus() == StatusUsuario.ATIVO)
                    .map(user -> user.getCredenciaisVersao() == number.longValue())
                    .orElse(false);
            if (!valid) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"erro\":\"Token inválido ou expirado\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
