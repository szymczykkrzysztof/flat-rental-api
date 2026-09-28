package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.dto.apartment.ApartmentCreateRequest;
import com.komy.flatrentalapi.dto.apartment.ApartmentMapper;
import com.komy.flatrentalapi.dto.apartment.ApartmentResponse;
import com.komy.flatrentalapi.dto.apartment.ApartmentUpdateRequest;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.entity.enums.ApartmentStatus;
import com.komy.flatrentalapi.entity.enums.Role;
import com.komy.flatrentalapi.exception.ResourceNotFoundException;
import com.komy.flatrentalapi.repository.ApartmentRepository;
import com.komy.flatrentalapi.repository.UserRepository;
import com.komy.flatrentalapi.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;
    private final ApartmentMapper apartmentMapper;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public ApartmentService(ApartmentRepository apartmentRepository, ApartmentMapper apartmentMapper, UserRepository userRepository, CurrentUserProvider currentUserProvider) {
        this.apartmentRepository = apartmentRepository;
        this.apartmentMapper = apartmentMapper;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public ApartmentResponse create(ApartmentCreateRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(request.ownerId())) {
            throw new AccessDeniedException("Cannot create an apartment on behalf of another owner");
        }

        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.ownerId()));

        var apartment = apartmentMapper.toEntity(request);
        apartment.setOwner(owner);
        apartment.setStatus(ApartmentStatus.AVAILABLE);
        apartment.setCreatedAt(Instant.now());

        var saved = apartmentRepository.save(apartment);
        return apartmentMapper.toResponse(saved);
    }

    public ApartmentResponse getById(Long id) {
        var apartmentEntity = apartmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + id));
        return apartmentMapper.toResponse(apartmentEntity);
    }

    public ApartmentResponse update(Long id, ApartmentUpdateRequest request) {
        var apartmentEntity = apartmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + id));
        requireOwnerOrAdmin(apartmentEntity.getOwner());
        apartmentMapper.updateEntityFromRequest(request, apartmentEntity);

        var updated = apartmentRepository.save(apartmentEntity);
        return apartmentMapper.toResponse(updated);
    }

    public void delete(Long id) {
        var apartmentEntity = apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + id));
        requireOwnerOrAdmin(apartmentEntity.getOwner());
        apartmentRepository.deleteById(id);
    }

    private void requireOwnerOrAdmin(User owner) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(owner.getId())) {
            throw new AccessDeniedException("You do not own this apartment");
        }
    }

    public Page<ApartmentResponse> getAll(Pageable pageable) {
        return apartmentRepository.findAll(pageable)
                .map(apartmentMapper::toResponse);
    }
}
