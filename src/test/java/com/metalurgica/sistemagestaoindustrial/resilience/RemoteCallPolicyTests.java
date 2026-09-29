package com.metalurgica.sistemagestaoindustrial.resilience;

import com.metalurgica.sistemagestaoindustrial.assistente.AssistenteErrorCode;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.AiProviderException;
import com.metalurgica.sistemagestaoindustrial.assistente.ai.*;
import com.metalurgica.sistemagestaoindustrial.observability.OperationalProperties;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RemoteCallPolicyTests {
    @Test
    void retriesOnlyTransientIdempotentReadsWithBoundedBackoff() {
        var properties = properties(3);
        var sleeps = new ArrayList<Duration>();
        var policy = new RemoteCallPolicy(properties, sleeps::add);
        var attempts = new AtomicInteger();

        String result = policy.execute(RemoteCallPolicy.Operation.READ_IDEMPOTENT, () -> {
            if (attempts.incrementAndGet() < 3) throw new RuntimeException(new SocketTimeoutException("timeout"));
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
        assertEquals(java.util.List.of(Duration.ofMillis(10), Duration.ofMillis(20)), sleeps);
        assertTrue(policy.shouldRetry(RemoteCallPolicy.Operation.READ_IDEMPOTENT,
                new RemoteHttpException(502, "bad gateway")));
        assertTrue(policy.shouldRetry(RemoteCallPolicy.Operation.READ_IDEMPOTENT,
                new RemoteHttpException(503, "unavailable")));
        assertTrue(policy.shouldRetry(RemoteCallPolicy.Operation.READ_IDEMPOTENT,
                new RemoteHttpException(504, "gateway timeout")));
    }

    @Test
    void doesNotRetry4xxWritesOrNonTransientDisabledProvider() {
        var attempts = new AtomicInteger();
        var policy = new RemoteCallPolicy(properties(3), duration -> fail("No backoff expected"));
        assertThrows(RemoteHttpException.class, () -> policy.execute(
                RemoteCallPolicy.Operation.READ_IDEMPOTENT, () -> {
                    attempts.incrementAndGet();
                    throw new RemoteHttpException(400, "bad request");
                }));
        assertEquals(1, attempts.get());

        attempts.set(0);
        assertThrows(RemoteHttpException.class, () -> policy.execute(
                RemoteCallPolicy.Operation.WRITE_NON_IDEMPOTENT, () -> {
                    attempts.incrementAndGet();
                    throw new RemoteHttpException(503, "unavailable");
                }));
        assertEquals(1, attempts.get());

        assertFalse(policy.shouldRetry(RemoteCallPolicy.Operation.READ_IDEMPOTENT,
                new AiProviderException(AssistenteErrorCode.AI_PROVIDER_UNAVAILABLE, "disabled", false)));
    }

    @Test
    void fakeAndDisabledGatewaysDoNotReceiveArtificialCircuitOrRetries() {
        var policy = new RemoteCallPolicy(properties(3), duration -> fail("No backoff expected"));
        var fake = new FakeChatModelGateway();
        policy.execute(RemoteCallPolicy.Operation.READ_IDEMPOTENT, () -> fake.complete(
                new ChatModelGateway.ChatModelRequest(java.util.List.of(), java.util.List.of(), 10)));
        assertEquals(1, fake.calls());

        var disabled = new DisabledChatModelGateway();
        assertThrows(AiProviderException.class, () -> policy.execute(RemoteCallPolicy.Operation.READ_IDEMPOTENT,
                () -> disabled.complete(new ChatModelGateway.ChatModelRequest(java.util.List.of(), java.util.List.of(), 10))));
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("io.github.resilience4j.circuitbreaker.CircuitBreaker"));
    }

    private OperationalProperties properties(int attempts) {
        var properties = new OperationalProperties();
        properties.setRemoteMaxAttempts(attempts);
        properties.setRemoteBackoff(Duration.ofMillis(10));
        return properties;
    }
}
