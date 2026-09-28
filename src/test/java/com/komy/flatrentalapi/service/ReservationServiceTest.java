package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.dto.reservation.ReservationCreateRequest;
import com.komy.flatrentalapi.dto.reservation.ReservationMapper;
import com.komy.flatrentalapi.dto.reservation.ReservationResponse;
import com.komy.flatrentalapi.entity.Apartment;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ApartmentRepository apartmentRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ReservationService reservationService;

    private final ReservationCreateRequest request = new ReservationCreateRequest(
            1L, LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 15)
    );

    private User userWithRole(Role role) {
        User user = new User();
        user.setId(1L);
        user.setRole(role);
        return user;
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"TENANT", "ADMIN"})
    void createAllowsTenantAndAdmin(Role role) {
        User currentUser = userWithRole(role);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        Apartment apartment = new Apartment();
        apartment.setId(1L);
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(reservationRepository.existsOverlappingReservation(1L, request.startDate(), request.endDate()))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationMapper.toResponse(any(Reservation.class))).thenReturn(
                new ReservationResponse(1L, 1L, currentUser.getId(), request.startDate(), request.endDate(), ReservationStatus.PENDING, null)
        );

        ReservationResponse response = reservationService.create(request);

        assertThat(response).isNotNull();
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertThat(captor.getValue().getTenant()).isEqualTo(currentUser);
        assertThat(captor.getValue().getApartment()).isEqualTo(apartment);
        assertThat(captor.getValue().getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void createDeniesOwner() {
        User currentUser = userWithRole(Role.OWNER);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(AccessDeniedException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createThrowsWhenApartmentNotFound() {
        User currentUser = userWithRole(Role.TENANT);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(apartmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createThrowsWhenStartDateNotBeforeEndDate() {
        User currentUser = userWithRole(Role.TENANT);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        var invalidRequest = new ReservationCreateRequest(1L, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 15));

        assertThatThrownBy(() -> reservationService.create(invalidRequest))
                .isInstanceOf(InvalidReservationDatesException.class);

        verify(apartmentRepository, never()).findById(any());
    }

    @Test
    void createThrowsWhenOverlapping() {
        User currentUser = userWithRole(Role.TENANT);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        Apartment apartment = new Apartment();
        apartment.setId(1L);
        when(apartmentRepository.findById(1L)).thenReturn(Optional.of(apartment));
        when(reservationRepository.existsOverlappingReservation(1L, request.startDate(), request.endDate()))
                .thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationConflictException.class);

        verify(reservationRepository, never()).save(any());
    }
}
