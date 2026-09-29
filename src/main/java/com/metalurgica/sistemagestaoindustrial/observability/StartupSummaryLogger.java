package com.metalurgica.sistemagestaoindustrial.observability;
import com.metalurgica.sistemagestaoindustrial.assistente.config.AiProperties;
import org.slf4j.*;
import org.springframework.boot.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
@Component
public class StartupSummaryLogger implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(StartupSummaryLogger.class);
    private final Environment environment; private final AiProperties aiProperties;
    public StartupSummaryLogger(Environment environment, AiProperties aiProperties) { this.environment = environment; this.aiProperties = aiProperties; }
    @Override public void run(ApplicationArguments args) {
        String profiles = String.join(",", environment.getActiveProfiles());
        LOG.info("application_started name={} version={} profiles={} java={} aiEnabled={}", environment.getProperty("spring.application.name", "application"), environment.getProperty("info.app.version", "unknown"), profiles.isBlank() ? "default" : LogSanitizer.safe(profiles), System.getProperty("java.version", "unknown"), aiProperties.isEnabled());
    }
}
