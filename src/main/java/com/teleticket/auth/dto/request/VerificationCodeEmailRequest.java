package com.luisalt20.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VerificationCodeEmailRequest {
    private String email;
    private String code;
    private boolean used;
    private LocalDateTime expiresAt;
}
