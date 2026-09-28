package com.komy.flatrentalapi.dto.apartment;

import jakarta.validation.constraints.NotBlank;

public record ApartmentCreateRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String city,
        @NotBlank String street,
        @NotBlank String postalCode
) {
}
