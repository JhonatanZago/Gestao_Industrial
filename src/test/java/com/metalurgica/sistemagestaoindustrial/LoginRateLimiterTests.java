package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.exception.RateLimitExceededException;
import com.metalurgica.sistemagestaoindustrial.security.LoginRateLimiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginRateLimiterTests {
    @Test
    void rejectsOnlyAfterConfiguredWindowBudget() {
        var limiter = new LoginRateLimiter(2);
        limiter.acquire("user@example.test", "127.0.0.1");
        limiter.acquire("user@example.test", "127.0.0.1");
        assertThrows(RateLimitExceededException.class,
            () -> limiter.acquire("user@example.test", "127.0.0.1"));
    }
}
