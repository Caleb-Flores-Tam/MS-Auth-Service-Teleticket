package com.luisalt20.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VerificationCodeEmailResponse {
    private Long id;

    private String email;

    private String code;

    private LocalDateTime createdAt;

    private boolean used;

    private LocalDateTime expiresAt;
}
