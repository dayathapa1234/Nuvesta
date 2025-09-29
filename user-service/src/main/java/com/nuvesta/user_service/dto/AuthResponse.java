package com.nuvesta.user_service.dto;

public record AuthResponse(String token, UserSummary user) {
}
