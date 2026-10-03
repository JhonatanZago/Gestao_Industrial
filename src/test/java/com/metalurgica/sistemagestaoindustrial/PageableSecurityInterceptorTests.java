package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.web.PageableSecurityInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageableSecurityInterceptorTests {
    private final PageableSecurityInterceptor interceptor = new PageableSecurityInterceptor();

    @Test
    void aceitaSortAllowlistado() {
        var request = new MockHttpServletRequest("GET", "/api/v1/empresas/1/usuarios");
        request.addParameter("sort", "nome,asc");
        assertDoesNotThrow(() -> interceptor.preHandle(request, null, null));
    }

    @Test
    void rejeitaSortArbitrario() {
        var request = new MockHttpServletRequest("GET", "/api/v1/empresas/1/usuarios");
        request.addParameter("sort", "nome desc,senhaHash");
        assertThrows(IllegalArgumentException.class, () -> interceptor.preHandle(request, null, null));
    }
}
