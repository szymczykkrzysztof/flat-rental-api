package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.dto.apartment.ApartmentCreateRequest;
import com.komy.flatrentalapi.dto.apartment.ApartmentMapper;
import com.komy.flatrentalapi.dto.apartment.ApartmentResponse;
import com.komy.flatrentalapi.entity.Apartment;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.entity.enums.ApartmentStatus;
import com.komy.flatrentalapi.entity.enums.Role;
import com.komy.flatrentalapi.repository.ApartmentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApartmentServiceTest {

    @Mock
    private ApartmentRepository apartmentRepository;
    @Mock
    private ApartmentMapper apartmentMapper;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private ApartmentService apartmentService;

    private final ApartmentCreateRequest request = new ApartmentCreateRequest(
            "Nice flat", "Spacious and bright", "Wrocław", "Testowa 1", "50-001"
    );

    private User userWithRole(Role role) {
        User user = new User();
        user.setId(1L);
        user.setRole(role);
        return user;
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"OWNER", "ADMIN"})
    void createAllowsOwnerAndAdmin(Role role) {
        User currentUser = userWithRole(role);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        Apartment mappedApartment = new Apartment();
        when(apartmentMapper.toEntity(request)).thenReturn(mappedApartment);
        when(apartmentRepository.save(any(Apartment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apartmentMapper.toResponse(any(Apartment.class))).thenReturn(
                new ApartmentResponse(1L, currentUser.getId(), "Nice flat", "Spacious and bright", "Wrocław", "Testowa 1", "50-001", "AVAILABLE", null)
        );

        ApartmentResponse response = apartmentService.create(request);

        assertThat(response).isNotNull();
        ArgumentCaptor<Apartment> captor = ArgumentCaptor.forClass(Apartment.class);
        verify(apartmentRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isEqualTo(currentUser);
        assertThat(captor.getValue().getStatus()).isEqualTo(ApartmentStatus.AVAILABLE);
    }

    @Test
    void createDeniesTenant() {
        User currentUser = userWithRole(Role.TENANT);
        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> apartmentService.create(request))
                .isInstanceOf(AccessDeniedException.class);

        verify(apartmentRepository, never()).save(any());
    }
}
