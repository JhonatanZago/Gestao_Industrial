package com.metalurgica.sistemagestaoindustrial.exception;

import com.metalurgica.sistemagestaoindustrial.observability.CorrelationContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTests {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request() {
        var request = new MockHttpServletRequest("GET", "/api/test");
        request.setAttribute(CorrelationContext.ATTRIBUTE, "corr-1");
        return request;
    }

    @Test
    void mapsStableClientAndBusinessCodes() {
        assertEquals(ErrorCode.VALIDATION_ERROR, handler.invalidRequest(new IllegalArgumentException("inválido"), request()).getBody().code());
        assertEquals(404, handler.notFound(new ResourceNotFoundException("ausente"), request()).getStatusCode().value());
        assertEquals(ErrorCode.ACCESS_DENIED, handler.accessDenied(new AccessDeniedException("não"), request()).getBody().code());
        assertEquals(409, handler.business(new BusinessException("regra"), request()).getStatusCode().value());
    }

    @Test
    void internalErrorIsGenericAndCorrelated() {
        var response = handler.internal(new RuntimeException("SQL password=C:/secret"), request());
        assertEquals(500, response.getStatusCode().value());
        assertEquals(ErrorCode.INTERNAL_ERROR, response.getBody().code());
        assertEquals("corr-1", response.getBody().correlationId());
        assertFalse(response.getBody().message().contains("SQL"));
        assertFalse(response.getBody().message().contains("C:/"));
    }
}
