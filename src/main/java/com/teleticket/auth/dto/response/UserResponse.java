package com.luisalt20.auth.dto.response;

import com.luisalt20.auth.dto.enumeration.UserStatus;

import java.util.List;

public record UserResponse(Long id, String email, UserStatus status, List<String> roles) {
}