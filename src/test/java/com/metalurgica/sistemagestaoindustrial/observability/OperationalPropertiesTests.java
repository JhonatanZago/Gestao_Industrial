package com.metalurgica.sistemagestaoindustrial.observability;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class OperationalPropertiesTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(TestConfig.class);

    @Test
    void defaultsAreFiniteAndConservative() {
        var properties = new OperationalProperties();
        properties.validate();
        assertEquals(10L * 1024 * 1024, properties.getMaxPayloadBytes());
        assertEquals(Duration.ofSeconds(30), properties.getShutdownTimeout());
        assertEquals(10, properties.getHikariMaximumPoolSize());
        assertEquals(2, properties.getHikariMinimumIdle());
        assertEquals(2, properties.getRemoteMaxAttempts());
        assertTrue(properties.isPrometheusEnabled());
    }

    @Test
    void rejectsUnsafeLimits() {
        var properties = new OperationalProperties();
        properties.setHikariMinimumIdle(11);
        assertThrows(IllegalStateException.class, properties::validate);
        properties.setHikariMinimumIdle(1);
        properties.setRemoteConnectTimeout(Duration.ZERO);
        assertThrows(IllegalStateException.class, properties::validate);
    }

    @Test
    void invalidBindingPreventsStartup() {
        contextRunner.withPropertyValues("operations.remote-max-attempts=0")
                .run(context -> assertTrue(context.getStartupFailure() != null));
    }

    @Test
    void productionProfileKeepsGlobalDebugAndSqlDisabled() {
        contextRunner.withPropertyValues("spring.profiles.active=prod")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertEquals("INFO", context.getEnvironment().getProperty("logging.level.root"));
                    assertEquals("false", context.getEnvironment().getProperty("spring.jpa.show-sql"));
                    assertEquals("graceful", context.getEnvironment().getProperty("server.shutdown"));
                    assertEquals("30s", context.getEnvironment().getProperty("spring.lifecycle.timeout-per-shutdown-phase"));
                    assertEquals("10", context.getEnvironment().getProperty("spring.datasource.hikari.maximum-pool-size"));
                    assertEquals("2", context.getEnvironment().getProperty("spring.datasource.hikari.minimum-idle"));
                    assertEquals("", context.getEnvironment().getProperty("security.jwt.secret"));
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(OperationalProperties.class)
    static class TestConfig {
    }
}
