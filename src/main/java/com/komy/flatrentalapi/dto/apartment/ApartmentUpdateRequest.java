package com.komy.flatrentalapi.dto.apartment;

import com.komy.flatrentalapi.entity.enums.ApartmentStatus;

public record ApartmentUpdateRequest(
        String title,
        String description,
        String city,
        String street,
        String postalCode,
        ApartmentStatus status
) {}
