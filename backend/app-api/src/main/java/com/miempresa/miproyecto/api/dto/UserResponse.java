package com.miempresa.miproyecto.api.dto;

import com.miempresa.miproyecto.core.domain.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
