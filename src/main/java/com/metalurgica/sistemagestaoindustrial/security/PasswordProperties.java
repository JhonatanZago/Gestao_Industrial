package com.metalurgica.sistemagestaoindustrial.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordProperties {
    private final int minLength;

    public PasswordProperties(@Value("${security.password.min-length:8}") int minLength) {
        this.minLength = minLength;
    }

    @PostConstruct
    void validate() {
        if (minLength < 8) {
            throw new IllegalStateException("PASSWORD_MIN_LENGTH não pode ser menor que 8");
        }
    }

    public int getMinLength() {
        return minLength;
    }
}
