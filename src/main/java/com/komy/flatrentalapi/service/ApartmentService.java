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
import com.komy.flatrentalapi.security.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;
    private final ApartmentMapper apartmentMapper;
    private final CurrentUserProvider currentUserProvider;

    public ApartmentService(ApartmentRepository apartmentRepository, ApartmentMapper apartmentMapper, CurrentUserProvider currentUserProvider) {
        this.apartmentRepository = apartmentRepository;
        this.apartmentMapper = apartmentMapper;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public ApartmentResponse create(ApartmentCreateRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Cannot create an apartment as tenant");
        }

        var apartment = apartmentMapper.toEntity(request);
        apartment.setOwner(currentUser);
        apartment.setStatus(ApartmentStatus.AVAILABLE);

        var saved = apartmentRepository.save(apartment);
        return apartmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApartmentResponse getById(Long id) {
        var apartmentEntity = apartmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + id));
        return apartmentMapper.toResponse(apartmentEntity);
    }

    @Transactional
    public ApartmentResponse update(Long id, ApartmentUpdateRequest request) {
        var apartmentEntity = apartmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + id));
        requireOwnerOrAdmin(apartmentEntity.getOwner());
        apartmentMapper.updateEntityFromRequest(request, apartmentEntity);

        var updated = apartmentRepository.save(apartmentEntity);
        return apartmentMapper.toResponse(updated);
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public Page<ApartmentResponse> getAll(Pageable pageable) {
        return apartmentRepository.findAll(pageable)
                .map(apartmentMapper::toResponse);
    }
}
