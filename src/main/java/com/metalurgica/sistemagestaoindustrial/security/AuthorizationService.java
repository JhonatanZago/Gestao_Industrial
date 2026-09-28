package com.metalurgica.sistemagestaoindustrial.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service("authorizationService")
public class AuthorizationService {
    public boolean hasPermission(Authentication authentication, String permission) {
        return authentication != null && authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).anyMatch(permission::equals);
    }

    public boolean belongsToCompany(Authentication authentication, Long companyId) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) return false;
        var claim = jwtAuthentication.getToken().getClaim("empresaId");
        return claim instanceof Number number && companyId.equals(number.longValue());
    }
}
