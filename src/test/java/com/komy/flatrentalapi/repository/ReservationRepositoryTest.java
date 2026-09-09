package com.komy.flatrentalapi.repository;

import com.komy.flatrentalapi.entity.Apartment;
import com.komy.flatrentalapi.entity.Reservation;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.entity.enums.ApartmentStatus;
import com.komy.flatrentalapi.entity.enums.ReservationStatus;
import com.komy.flatrentalapi.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Testcontainers
public class ReservationRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User tenant;
    private Apartment apartment;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setFirstName("Jan");
        owner.setLastName("Kowalski");
        owner.setPasswordHash("hash");
        owner.setRole(Role.OWNER);
        userRepository.save(owner);

        apartment = new Apartment();
        apartment.setTitle("Test");
        apartment.setDescription("Test");
        apartment.setCity("Wrocław");
        apartment.setStreet("Testowa 1");
        apartment.setPostalCode("50-001");
        apartment.setStatus(ApartmentStatus.AVAILABLE);
        apartment.setOwner(owner);
        apartmentRepository.save(apartment);

        tenant = new User();
        tenant.setEmail("tenant@test.com");
        tenant.setFirstName("Anna");
        tenant.setLastName("Nowak");
        tenant.setPasswordHash("hash");
        tenant.setRole(Role.TENANT);
        userRepository.save(tenant);
    }

    @Test
    void shouldSaveAndRetrieveReservation() {
        var reservation = new Reservation();
        reservation.setTenant(tenant);
        reservation.setApartment(apartment);
        reservation.setStartDate(LocalDate.now());
        reservation.setEndDate(LocalDate.now().plusDays(2));
        reservation.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(reservation);
        entityManager.flush();
        entityManager.clear();
        var savedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(savedReservation).isEqualTo(reservation);
        assertThat(savedReservation.getId()).isEqualTo(reservation.getId());
        assertThat(savedReservation.getApartment()).isEqualTo(reservation.getApartment());
        assertThat(savedReservation.getTenant()).isEqualTo(reservation.getTenant());
        assertThat(savedReservation.getStartDate()).isEqualTo(reservation.getStartDate());
        assertThat(savedReservation.getEndDate()).isEqualTo(reservation.getEndDate());
        assertThat(savedReservation.getStatus()).isEqualTo(reservation.getStatus());
    }

    @Test
    void shouldDetectOverlappingReservation() {
        var existingReservation = new Reservation();
        existingReservation.setApartment(apartment);
        existingReservation.setTenant(tenant);
        existingReservation.setStartDate(LocalDate.of(2026, 6, 10));
        existingReservation.setEndDate(LocalDate.of(2026, 6, 15));
        existingReservation.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(existingReservation);
        boolean overlaps = reservationRepository.existsOverlappingReservation(
                apartment.getId(),
                LocalDate.of(2026, 6, 12),
                LocalDate.of(2026, 6, 18)
        );
        assertThat(overlaps).isTrue();
    }

    @Test
    void shouldNotDetectOverlappingReservation() {
        var existingReservation = new Reservation();
        existingReservation.setApartment(apartment);
        existingReservation.setTenant(tenant);
        existingReservation.setStartDate(LocalDate.of(2026, 6, 10));
        existingReservation.setEndDate(LocalDate.of(2026, 6, 15));
        existingReservation.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(existingReservation);
        boolean overlaps = reservationRepository.existsOverlappingReservation(
                apartment.getId(),
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 20)
        );
        assertThat(overlaps).isFalse();
    }

    @Test
    void shouldNotDetectOverlappingReservationWhenCancelled() {
        var existingReservation = new Reservation();
        existingReservation.setApartment(apartment);
        existingReservation.setTenant(tenant);
        existingReservation.setStartDate(LocalDate.of(2026, 6, 10));
        existingReservation.setEndDate(LocalDate.of(2026, 6, 15));
        existingReservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(existingReservation);
        boolean overlaps = reservationRepository.existsOverlappingReservation(
                apartment.getId(),
                LocalDate.of(2026, 6, 12),
                LocalDate.of(2026, 6, 18)
        );
        assertThat(overlaps).isFalse();
    }
}
