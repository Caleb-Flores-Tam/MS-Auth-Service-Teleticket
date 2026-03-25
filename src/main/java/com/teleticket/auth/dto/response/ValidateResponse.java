package com.teleticket.auth.dto.response;

import java.util.Map;

public record ValidateResponse(boolean valid, Map<String, Object> claims) {
}