package com.komy.flatrentalapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApartmentCreateRequest(
        @NotNull Long ownerId,
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String city,
        @NotBlank String street,
        @NotBlank String postalCode
) {
}
