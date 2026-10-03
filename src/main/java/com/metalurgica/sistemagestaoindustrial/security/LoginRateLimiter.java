package com.metalurgica.sistemagestaoindustrial.security;

import com.metalurgica.sistemagestaoindustrial.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {
    private final int maxAttemptsPerMinute;
    private final Clock clock = Clock.systemUTC();
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public LoginRateLimiter(@Value("${security.login.max-attempts-per-minute:10}") int maxAttemptsPerMinute) {
        if (maxAttemptsPerMinute < 1) throw new IllegalArgumentException("login rate limit must be positive");
        this.maxAttemptsPerMinute = maxAttemptsPerMinute;
    }

    public void acquire(String email, String remoteAddress) {
        var minute = clock.instant().getEpochSecond() / 60;
        var key = normalize(remoteAddress) + ":" + normalize(email);
        var window = windows.compute(key, (ignored, current) ->
            current == null || current.minute != minute ? new Window(minute, 1) : new Window(minute, current.count + 1));
        if (window.count > maxAttemptsPerMinute) throw new RateLimitExceededException("login rate limit exceeded");
        if (windows.size() > 4096) windows.entrySet().removeIf(entry -> entry.getValue().minute < minute - 1);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? "unknown" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private record Window(long minute, int count) { }
}
