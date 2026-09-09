package com.komy.flatrentalapi.dto.reservation;

import com.komy.flatrentalapi.entity.enums.ReservationStatus;

import java.time.Instant;
import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        Long apartmentId,
        Long tenantId,
        LocalDate startDate,
        LocalDate endDate,
        ReservationStatus status,
        Instant createdAt
) {
}
