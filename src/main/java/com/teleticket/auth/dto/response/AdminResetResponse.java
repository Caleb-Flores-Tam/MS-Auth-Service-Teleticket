package com.teleticket.auth.dto.response;

public record AdminResetResponse(
        String email,
        boolean passwordReset
) {
}
