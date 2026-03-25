package com.luisalt20.auth.dto.request;
import jakarta.validation.constraints.NotBlank;
public record ValidateRequest(@NotBlank String token) {}