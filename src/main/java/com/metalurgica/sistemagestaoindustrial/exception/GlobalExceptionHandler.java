package com.metalurgica.sistemagestaoindustrial.exception;

import com.metalurgica.sistemagestaoindustrial.assistente.ai.AiProviderException;
import com.metalurgica.sistemagestaoindustrial.assistente.tool.AiToolException;
import com.metalurgica.sistemagestaoindustrial.observability.CorrelationContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException ex, HttpServletRequest req) { return response(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, "Acesso negado.", req); }
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> badCredentials(BadCredentialsException ex, HttpServletRequest req) { return response(HttpStatus.UNAUTHORIZED, ErrorCode.ACCESS_DENIED, "Credenciais inválidas.", req); }
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest req) { return response(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), req); }
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> noResource(NoResourceFoundException ex, HttpServletRequest req) { return response(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, "Recurso não encontrado.", req); }
    @ExceptionHandler({DuplicateResourceException.class, BusinessException.class, DataIntegrityViolationException.class})
    ResponseEntity<ErrorResponse> business(Exception ex, HttpServletRequest req) {
        String message = ex instanceof DataIntegrityViolationException ? "Recurso duplicado ou relacionamento inválido." : ex.getMessage();
        return response(HttpStatus.CONFLICT, ErrorCode.BUSINESS_RULE_VIOLATION, message, req);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var fields = new LinkedHashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErrorResponse(java.time.OffsetDateTime.now(), 400, ErrorCode.VALIDATION_ERROR,
                "Dados inválidos.", req.getRequestURI(), CorrelationContext.from(req), fields));
    }
    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class, MultipartException.class, MissingServletRequestPartException.class})
    ResponseEntity<ErrorResponse> invalidRequest(Exception ex, HttpServletRequest req) {
        String message = ex instanceof IllegalArgumentException ? ex.getMessage()
                : ex instanceof MissingServletRequestParameterException || ex instanceof MethodArgumentTypeMismatchException
                ? "Parâmetro de consulta inválido ou ausente." : "Requisição inválida.";
        return response(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, req);
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.RESOURCE_NOT_FOUND,
                "Operação não suportada.", req);
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErrorResponse> uploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCode.VALIDATION_ERROR,
                "Payload excede o limite permitido.", req);
    }
    @ExceptionHandler(AiToolException.class)
    ResponseEntity<ErrorResponse> aiTool(AiToolException ex, HttpServletRequest req) {
        HttpStatus status = ex.getCode().name().contains("PERMISSION") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
        return response(status, status == HttpStatus.FORBIDDEN ? ErrorCode.ACCESS_DENIED : ErrorCode.BUSINESS_RULE_VIOLATION, ex.getMessage(), req);
    }
    @ExceptionHandler(AiProviderException.class)
    ResponseEntity<ErrorResponse> aiProvider(AiProviderException ex, HttpServletRequest req) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.AI_PROVIDER_UNAVAILABLE, "Provedor de IA indisponível.", req);
    }
    @ExceptionHandler(ImportProcessingException.class)
    ResponseEntity<ErrorResponse> importFailure(ImportProcessingException ex, HttpServletRequest req) {
        LOG.error("import_processing_error path={}", req.getRequestURI(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.IMPORT_ERROR,
                "Falha técnica ao processar importação. Informe o correlation ID ao suporte.", req);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> internal(Exception ex, HttpServletRequest req) {
        LOG.error("unhandled_request_error path={}", req.getRequestURI(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Erro interno. Informe o correlation ID ao suporte.", req);
    }
    private ResponseEntity<ErrorResponse> response(HttpStatus status, ErrorCode code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), code, message, request.getRequestURI(), CorrelationContext.from(request)));
    }
}
