package com.komy.flatrentalapi.dto.auth;

import com.komy.flatrentalapi.entity.enums.Role;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role) {
}
