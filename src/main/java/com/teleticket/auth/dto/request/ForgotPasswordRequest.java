package com.teleticket.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest{
    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    private String email;
}