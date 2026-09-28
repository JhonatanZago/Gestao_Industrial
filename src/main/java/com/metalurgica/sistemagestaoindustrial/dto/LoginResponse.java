package com.metalurgica.sistemagestaoindustrial.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {}
