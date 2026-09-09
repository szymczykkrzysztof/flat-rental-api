package com.komy.flatrentalapi.repository;

import com.komy.flatrentalapi.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.apartment.id = :apartmentId
            AND r.status IN ('PENDING', 'CONFIRMED')
            AND r.startDate < :endDate
            AND r.endDate > :startDate
            """)
    boolean existsOverlappingReservation(
            @Param("apartmentId") Long apartmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
