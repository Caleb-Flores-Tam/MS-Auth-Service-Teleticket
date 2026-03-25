package com.teleticket.auth.dto.response;

public record TokenResponse(String tokenType, String accessToken, long expiresInSeconds, String refreshToken) {
}