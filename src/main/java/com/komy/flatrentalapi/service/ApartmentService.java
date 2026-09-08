package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.dto.ApartmentCreateRequest;
import com.komy.flatrentalapi.dto.ApartmentMapper;
import com.komy.flatrentalapi.dto.ApartmentResponse;
import com.komy.flatrentalapi.dto.ApartmentUpdateRequest;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.entity.enums.ApartmentStatus;
import com.komy.flatrentalapi.exception.ResourceNotFoundException;
import com.komy.flatrentalapi.repository.ApartmentRepository;
import com.komy.flatrentalapi.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;
    private final ApartmentMapper apartmentMapper;
    private final UserRepository userRepository;

    public ApartmentService(ApartmentRepository apartmentRepository, ApartmentMapper apartmentMapper, UserRepository userRepository) {
        this.apartmentRepository = apartmentRepository;
        this.apartmentMapper = apartmentMapper;
        this.userRepository = userRepository;
    }

    public ApartmentResponse create(ApartmentCreateRequest request) {
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
        apartmentMapper.updateEntityFromRequest(request, apartmentEntity);

        var updated = apartmentRepository.save(apartmentEntity);
        return apartmentMapper.toResponse(updated);
    }

    public void delete(Long id) {
        if (!apartmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Apartment not found with id: " + id);
        }
        apartmentRepository.deleteById(id);
    }

    public Page<ApartmentResponse> getAll(Pageable pageable) {
        return apartmentRepository.findAll(pageable)
                .map(apartmentMapper::toResponse);
    }
}
