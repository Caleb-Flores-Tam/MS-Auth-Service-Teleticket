package com.luisalt20.auth.dto.response;

public record AdminResetResponse(
        String email,
        boolean passwordReset
) {
}
