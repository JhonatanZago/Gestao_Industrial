package com.metalurgica.sistemagestaoindustrial.observability;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LogSanitizerTests {
    @Test void removesControlCharactersAndSecrets() {
        assertEquals("line_one", LogSanitizer.safe("line\none")); assertEquals("[REDACTED]", LogSanitizer.safe("Authorization: Bearer token.value")); assertTrue(LogSanitizer.safe("x".repeat(500)).length() <= 200);
    }
}
