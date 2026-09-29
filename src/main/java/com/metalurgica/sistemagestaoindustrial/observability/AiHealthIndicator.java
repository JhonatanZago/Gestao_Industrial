package com.metalurgica.sistemagestaoindustrial.observability;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
@Component("ai")
public class AiHealthIndicator implements HealthIndicator {
    private final AiAvailabilityService availability;
    public AiHealthIndicator(AiAvailabilityService availability) { this.availability = availability; }
    @Override public Health health() { return Health.up().withDetail("chat", availability.chat().name()).withDetail("rag", availability.rag().name()).build(); }
}

