package com.metalurgica.sistemagestaoindustrial.observability;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "operations")
public class OperationalProperties {
    private long maxPayloadBytes = 10L * 1024 * 1024;
    private Duration shutdownTimeout = Duration.ofSeconds(30);
    private boolean prometheusEnabled = true;
    private int hikariMaximumPoolSize = 10;
    private int hikariMinimumIdle = 2;
    private Duration hikariConnectionTimeout = Duration.ofSeconds(30);
    private Duration hikariIdleTimeout = Duration.ofMinutes(10);
    private Duration hikariMaxLifetime = Duration.ofMinutes(30);
    private Duration remoteConnectTimeout = Duration.ofSeconds(2);
    private Duration remoteResponseTimeout = Duration.ofSeconds(30);
    private int remoteMaxAttempts = 2;
    private Duration remoteBackoff = Duration.ofMillis(200);
    private int remoteCircuitFailureThreshold = 5;
    private Duration remoteCircuitOpenDuration = Duration.ofSeconds(30);

    @PostConstruct
    public void validate() {
        if (maxPayloadBytes < 1 || !positive(shutdownTimeout)
                || hikariMaximumPoolSize < 1 || hikariMinimumIdle < 0
                || hikariMinimumIdle > hikariMaximumPoolSize
                || !positive(hikariConnectionTimeout) || !positive(hikariIdleTimeout)
                || !positive(hikariMaxLifetime) || !positive(remoteConnectTimeout)
                || !positive(remoteResponseTimeout) || remoteMaxAttempts < 1
                || remoteMaxAttempts > 5 || remoteBackoff == null || remoteBackoff.isNegative()
                || remoteCircuitFailureThreshold < 1 || !positive(remoteCircuitOpenDuration)) {
            throw new IllegalStateException("Configuração operacional inválida.");
        }
    }

    private boolean positive(Duration value) {
        return value != null && !value.isNegative() && !value.isZero();
    }

    public long getMaxPayloadBytes() { return maxPayloadBytes; }
    public void setMaxPayloadBytes(long value) { this.maxPayloadBytes = value; }
    public Duration getShutdownTimeout() { return shutdownTimeout; }
    public void setShutdownTimeout(Duration value) { this.shutdownTimeout = value; }
    public boolean isPrometheusEnabled() { return prometheusEnabled; }
    public void setPrometheusEnabled(boolean value) { this.prometheusEnabled = value; }
    public int getHikariMaximumPoolSize() { return hikariMaximumPoolSize; }
    public void setHikariMaximumPoolSize(int value) { this.hikariMaximumPoolSize = value; }
    public int getHikariMinimumIdle() { return hikariMinimumIdle; }
    public void setHikariMinimumIdle(int value) { this.hikariMinimumIdle = value; }
    public Duration getHikariConnectionTimeout() { return hikariConnectionTimeout; }
    public void setHikariConnectionTimeout(Duration value) { this.hikariConnectionTimeout = value; }
    public Duration getHikariIdleTimeout() { return hikariIdleTimeout; }
    public void setHikariIdleTimeout(Duration value) { this.hikariIdleTimeout = value; }
    public Duration getHikariMaxLifetime() { return hikariMaxLifetime; }
    public void setHikariMaxLifetime(Duration value) { this.hikariMaxLifetime = value; }
    public Duration getRemoteConnectTimeout() { return remoteConnectTimeout; }
    public void setRemoteConnectTimeout(Duration value) { this.remoteConnectTimeout = value; }
    public Duration getRemoteResponseTimeout() { return remoteResponseTimeout; }
    public void setRemoteResponseTimeout(Duration value) { this.remoteResponseTimeout = value; }
    public int getRemoteMaxAttempts() { return remoteMaxAttempts; }
    public void setRemoteMaxAttempts(int value) { this.remoteMaxAttempts = value; }
    public Duration getRemoteBackoff() { return remoteBackoff; }
    public void setRemoteBackoff(Duration value) { this.remoteBackoff = value; }
    public int getRemoteCircuitFailureThreshold() { return remoteCircuitFailureThreshold; }
    public void setRemoteCircuitFailureThreshold(int value) { this.remoteCircuitFailureThreshold = value; }
    public Duration getRemoteCircuitOpenDuration() { return remoteCircuitOpenDuration; }
    public void setRemoteCircuitOpenDuration(Duration value) { this.remoteCircuitOpenDuration = value; }
}

