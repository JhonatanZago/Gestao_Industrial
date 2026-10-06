package com.metalurgica.sistemagestaoindustrial.importacao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "industrial.importacao.automatica")
public class AutomaticImportProperties {
    private boolean enabled = false;
    private boolean dryRun = false;
    private InitialMode initialMode = InitialMode.IGNORE_EXISTING;
    private long debounceMs = 3000;
    private Stability stability = new Stability();
    private Reconciliation reconciliation = new Reconciliation();
    private List<Directory> directories = new ArrayList<>();

    @Getter @Setter
    public static class Reconciliation {
        private boolean enabled = true;
        private String cron = "0 0 18 * * *";
        private String zone = "America/Sao_Paulo";
    }

    @Getter @Setter
    public static class Stability {
        private long intervalMs = 1000;
        private int stableChecks = 3;
        private long timeoutSeconds = 60;
    }

    @Getter @Setter
    public static class Directory {
        private Long empresaId;
        private Long setorId;
        private String path;
        private SourceKind kind = SourceKind.DIRECTORY;
    }

    public enum SourceKind { DIRECTORY, FILE }
    public enum InitialMode { IGNORE_EXISTING, PROCESS_EXISTING, DRY_RUN }
}
