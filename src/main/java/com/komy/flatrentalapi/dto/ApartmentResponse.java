package com.komy.flatrentalapi.dto;

import java.time.Instant;

public record ApartmentResponse(
        Long id,
        Long ownerId,
        String title,
        String description,
        String city,
        String street,
        String postalCode,
        String status,
        Instant createdAt

) {
}
