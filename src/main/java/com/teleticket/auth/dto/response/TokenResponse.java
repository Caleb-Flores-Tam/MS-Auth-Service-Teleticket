package com.luisalt20.auth.dto.response;

public record TokenResponse(String tokenType, String accessToken, long expiresInSeconds, String refreshToken) {
}