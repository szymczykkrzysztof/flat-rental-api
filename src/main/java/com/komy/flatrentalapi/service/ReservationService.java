package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.dto.reservation.ReservationCreateRequest;
import com.komy.flatrentalapi.dto.reservation.ReservationMapper;
import com.komy.flatrentalapi.dto.reservation.ReservationResponse;
import com.komy.flatrentalapi.entity.Reservation;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.entity.enums.ReservationStatus;
import com.komy.flatrentalapi.entity.enums.Role;
import com.komy.flatrentalapi.exception.InvalidReservationDatesException;
import com.komy.flatrentalapi.exception.ReservationConflictException;
import com.komy.flatrentalapi.exception.ResourceNotFoundException;
import com.komy.flatrentalapi.repository.ApartmentRepository;
import com.komy.flatrentalapi.repository.ReservationRepository;
import com.komy.flatrentalapi.security.CurrentUserProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {
    private final ApartmentRepository apartmentRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final CurrentUserProvider currentUserProvider;

    public ReservationService(ApartmentRepository apartmentRepository, ReservationRepository reservationRepository, ReservationMapper reservationMapper, CurrentUserProvider currentUserProvider) {
        this.apartmentRepository = apartmentRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.currentUserProvider = currentUserProvider;
    }

    public ReservationResponse create(ReservationCreateRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.TENANT) {
            throw new AccessDeniedException("Cannot create a reservation as owner");
        }

        if (!request.startDate().isBefore(request.endDate())) {
            throw new InvalidReservationDatesException("Start date must be before end date");
        }

        var apartment = apartmentRepository.findById(request.apartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + request.apartmentId()));

        if (reservationRepository.existsOverlappingReservation(request.apartmentId(), request.startDate(), request.endDate())) {
            throw new ReservationConflictException("Overlapping reservation");
        }

        var reservation = new Reservation();
        reservation.setApartment(apartment);
        reservation.setTenant(currentUser);
        reservation.setStartDate(request.startDate());
        reservation.setEndDate(request.endDate());
        reservation.setStatus(ReservationStatus.PENDING);
        var saved = reservationRepository.save(reservation);

        return reservationMapper.toResponse(saved);
    }

}
