package com.komy.flatrentalapi.dto.reservation;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationCreateRequest(
        @NotNull Long apartmentId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
        ) {
}
