package com.teleticket.auth.dto.response;

import com.teleticket.auth.dto.enumeration.UserStatus;

import java.util.List;

public record UserResponse(Long id, String email, UserStatus status, List<String> roles) {
}