package com.luisalt20.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "El token es requerido")
    private String resetToken;

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 8, max = 72)
    private String newPassword;
}