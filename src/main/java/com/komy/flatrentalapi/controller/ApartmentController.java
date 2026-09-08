package com.komy.flatrentalapi.controller;

import com.komy.flatrentalapi.dto.ApartmentCreateRequest;
import com.komy.flatrentalapi.dto.ApartmentResponse;
import com.komy.flatrentalapi.dto.ApartmentUpdateRequest;
import com.komy.flatrentalapi.service.ApartmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/apartments")
public class ApartmentController {
    private final ApartmentService apartmentService;

    public ApartmentController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @GetMapping
    public ResponseEntity<Page<ApartmentResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(apartmentService.getAll(pageable));
    }

    @PostMapping
    public ResponseEntity<ApartmentResponse> create(@RequestBody @Valid ApartmentCreateRequest request) {
        ApartmentResponse response = apartmentService.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<ApartmentResponse> getById(@PathVariable Long id) {
        ApartmentResponse response = apartmentService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("{id}")
    public ResponseEntity<ApartmentResponse> updateById(@PathVariable Long id, @RequestBody @Valid ApartmentUpdateRequest request) {
        ApartmentResponse response = apartmentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {

        apartmentService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
