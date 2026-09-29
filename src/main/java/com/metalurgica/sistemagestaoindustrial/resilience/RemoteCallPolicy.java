package com.metalurgica.sistemagestaoindustrial.resilience;

import com.metalurgica.sistemagestaoindustrial.assistente.ai.AiProviderException;
import com.metalurgica.sistemagestaoindustrial.observability.OperationalProperties;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

@Component
public class RemoteCallPolicy {
    public enum Operation { READ_IDEMPOTENT, WRITE_NON_IDEMPOTENT }
    @FunctionalInterface public interface Sleeper { void sleep(Duration duration) throws InterruptedException; }
    private static final Set<Integer> TRANSIENT_STATUS = Set.of(502, 503, 504);
    private final OperationalProperties properties;
    private final Sleeper sleeper;

    @Autowired
    public RemoteCallPolicy(OperationalProperties properties) {
        this(properties, duration -> Thread.sleep(duration.toMillis()));
    }

    RemoteCallPolicy(OperationalProperties properties, Sleeper sleeper) {
        this.properties = properties;
        this.sleeper = sleeper;
    }

    public <T> T execute(Operation operation, Supplier<T> call) {
        RuntimeException last;
        for (int attempt = 1; ; attempt++) {
            try {
                return call.get();
            } catch (RuntimeException exception) {
                last = exception;
                if (attempt >= properties.getRemoteMaxAttempts() || !shouldRetry(operation, exception)) throw last;
                backoff(attempt);
            }
        }
    }

    public boolean shouldRetry(Operation operation, Throwable failure) {
        if (operation != Operation.READ_IDEMPOTENT) return false;
        Throwable current = failure;
        while (current != null) {
            if (current instanceof SocketTimeoutException || current instanceof HttpTimeoutException
                    || current instanceof ConnectException) return true;
            if (current instanceof RemoteHttpException http && TRANSIENT_STATUS.contains(http.getStatus())) return true;
            if (current instanceof AiProviderException provider && provider.isTransientFailure()) return true;
            current = current.getCause();
        }
        return false;
    }

    private void backoff(int attempt) {
        long base = properties.getRemoteBackoff().toMillis();
        long factor = 1L << Math.min(attempt - 1, 4);
        try {
            sleeper.sleep(Duration.ofMillis(Math.min(base * factor, 5_000)));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry remoto interrompido.", exception);
        }
    }
}
