package com.metalurgica.sistemagestaoindustrial.resource;
import com.metalurgica.sistemagestaoindustrial.observability.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
@RestController @RequestMapping("/api/v1/operacao/status")
public class OperationalStatusResource {
    private final DataSource dataSource; private final AiAvailabilityService ai;
    public OperationalStatusResource(DataSource dataSource, AiAvailabilityService ai) { this.dataSource = dataSource; this.ai = ai; }
    @GetMapping @PreAuthorize("hasAuthority('OPERACAO_MONITORAR')")
    public StatusResponse status() { return new StatusResponse(databaseAvailable(), ai.chat(), ai.rag()); }
    private boolean databaseAvailable() { try (var connection = dataSource.getConnection()) { return connection.isValid(1); } catch (Exception ignored) { return false; } }
    public record StatusResponse(boolean databaseAvailable, AiOperationalState ai, AiOperationalState rag) { }
}

