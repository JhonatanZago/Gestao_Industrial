package com.metalurgica.sistemagestaoindustrial.security;

import com.metalurgica.sistemagestaoindustrial.domain.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

@Service
public class JwtTokenService {
    private final JwtProperties properties;
    private final JwtEncoder encoder;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        var key = new SecretKeySpec(properties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = org.springframework.security.oauth2.jwt.NimbusJwtEncoder.withSecretKey(key).build();
    }

    public String issue(Usuario user) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
            .subject(user.getId().toString())
            .claim("usuarioId", user.getId())
            .claim("empresaId", user.getEmpresa().getId())
            .claim("perfil", user.getPerfil().getCodigo())
            .claim("permissoes", user.getPerfil().getPermissoes().stream().map(p -> p.getCodigo()).toList())
            .claim("credenciaisVersao", user.getCredenciaisVersao())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(properties.getExpirationSeconds()))
            .build();
        var headers = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return properties.getExpirationSeconds();
    }
}
