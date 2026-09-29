package com.metalurgica.sistemagestaoindustrial.security;
import com.metalurgica.sistemagestaoindustrial.exception.*;
import com.metalurgica.sistemagestaoindustrial.observability.CorrelationContext;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
@Component
public class SecurityErrorWriter {
    private final ObjectMapper objectMapper;
    public SecurityErrorWriter(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) throws IOException {
        write(request, response, status, ErrorCode.ACCESS_DENIED, message);
    }
    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, ErrorCode code, String message) throws IOException {
        response.setStatus(status.value()); response.setContentType("application/json"); response.setCharacterEncoding("UTF-8"); response.setHeader(CorrelationContext.HEADER, CorrelationContext.from(request));
        objectMapper.writeValue(response.getOutputStream(), ErrorResponse.of(status.value(), code, message, request.getRequestURI(), CorrelationContext.from(request)));
    }
}
