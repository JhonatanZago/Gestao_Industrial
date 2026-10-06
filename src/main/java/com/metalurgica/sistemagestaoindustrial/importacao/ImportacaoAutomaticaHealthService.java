package com.metalurgica.sistemagestaoindustrial.importacao;

import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ImportacaoAutomaticaHealthService {
    private final AutomaticImportProperties properties;
    private volatile OffsetDateTime lastCheck;
    public ImportacaoAutomaticaHealthService(AutomaticImportProperties properties) { this.properties = properties; }
    public List<SourceHealth> check() {
        lastCheck = OffsetDateTime.now();
        return properties.getDirectories().stream().map(source -> {
            boolean configured = source.getEmpresaId() != null && source.getSetorId() != null && source.getPath() != null && !source.getPath().isBlank();
            boolean accessible = false;
            if (configured) try { accessible = Files.isReadable(Path.of(source.getPath())); } catch (RuntimeException ignored) { }
            return new SourceHealth(source.getEmpresaId(), source.getSetorId(), configured, accessible,
                    properties.isEnabled() && accessible, lastCheck, accessible ? "OK" : "INDISPONIVEL");
        }).toList();
    }
    public OffsetDateTime lastCheck() { return lastCheck; }
    public record SourceHealth(Long empresaId, Long setorId, boolean configurado, boolean acessivel,
                               boolean monitoramentoAtivo, OffsetDateTime ultimoCheck, String mensagem) { }
}
