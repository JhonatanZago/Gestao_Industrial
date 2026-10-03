package com.metalurgica.sistemagestaoindustrial.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.metalurgica.sistemagestaoindustrial.repository.UsuarioRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final UsuarioRepository usuarioRepository;
    private final AuthorizationService authorizationService;
    private final SectorScopeService sectorScopeService;
    private final String allowedOrigins;
    private final SecurityErrorWriter securityErrorWriter;

    public SecurityConfig(JwtProperties jwtProperties,
                          UsuarioRepository usuarioRepository,
                          AuthorizationService authorizationService,
                          SectorScopeService sectorScopeService,
                          SecurityErrorWriter securityErrorWriter,
                          @Value("${security.cors.allowed-origins}") String allowedOrigins) {
        this.jwtProperties = jwtProperties;
        this.usuarioRepository = usuarioRepository;
        this.authorizationService = authorizationService;
        this.sectorScopeService = sectorScopeService;
        this.securityErrorWriter = securityErrorWriter;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .contentTypeOptions(contentType -> { })
                .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .frameOptions(frame -> frame.deny())
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"))
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/v1/auth/login", "/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness").permitAll()
                .requestMatchers("/actuator/info", "/actuator/metrics/**", "/actuator/prometheus", "/api/v1/operacao/**").hasAuthority("OPERACAO_MONITORAR")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .decoder(jwtDecoder())
                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(this::writeUnauthorized))
            .addFilterAfter(new CredentialVersionFilter(usuarioRepository), BearerTokenAuthenticationFilter.class)
            .addFilterAfter(new RequestAuthorizationFilter(authorizationService, sectorScopeService, securityErrorWriter), CredentialVersionFilter.class)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(this::writeUnauthorized)
                .accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        var key = new SecretKeySpec(jwtProperties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("permissoes");
        authorities.setAuthorityPrefix("");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toList();
        if (origins.isEmpty() || origins.stream().anyMatch("*"::equals)) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS deve listar origens explícitas");
        }

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "X-Correlation-Id"));
        configuration.setExposedHeaders(List.of("Authorization", "X-Correlation-Id", "Content-Disposition"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {
            securityErrorWriter.write(request, response, HttpStatus.FORBIDDEN, "Acesso negado.");
        };
    }

    private void writeUnauthorized(jakarta.servlet.http.HttpServletRequest request,
                                   jakarta.servlet.http.HttpServletResponse response,
                                   Exception exception) throws java.io.IOException {
        securityErrorWriter.write(request, response, HttpStatus.UNAUTHORIZED, "Autenticação necessária.");
    }
}
