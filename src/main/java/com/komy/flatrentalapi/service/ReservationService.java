package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.dto.reservation.ReservationCreateRequest;
import com.komy.flatrentalapi.dto.reservation.ReservationMapper;
import com.komy.flatrentalapi.dto.reservation.ReservationResponse;
import com.komy.flatrentalapi.entity.Reservation;
import com.komy.flatrentalapi.entity.enums.ReservationStatus;
import com.komy.flatrentalapi.exception.InvalidReservationDatesException;
import com.komy.flatrentalapi.exception.ReservationConflictException;
import com.komy.flatrentalapi.exception.ResourceNotFoundException;
import com.komy.flatrentalapi.repository.ApartmentRepository;
import com.komy.flatrentalapi.repository.ReservationRepository;
import com.komy.flatrentalapi.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public ReservationService(ApartmentRepository apartmentRepository, UserRepository userRepository, ReservationRepository reservationRepository, ReservationMapper reservationMapper) {
        this.apartmentRepository = apartmentRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    public ReservationResponse create(ReservationCreateRequest request) {
        var apartment = apartmentRepository.findById(request.apartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + request.apartmentId()));
        var tenant = userRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.tenantId()));

        if (!request.startDate().isBefore(request.endDate())) {
            throw new InvalidReservationDatesException("Start date must be before end date");
        }

        if (reservationRepository.existsOverlappingReservation(request.apartmentId(), request.startDate(), request.endDate())) {
            throw new ReservationConflictException("Overlapping reservation");
        }

        var reservation = new Reservation();
        reservation.setApartment(apartment);
        reservation.setTenant(tenant);
        reservation.setStartDate(request.startDate());
        reservation.setEndDate(request.endDate());
        reservation.setStatus(ReservationStatus.PENDING);
        var saved = reservationRepository.save(reservation);

        return reservationMapper.toResponse(saved);
    }

}
