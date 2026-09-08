package com.komy.flatrentalapi.repository;

import com.komy.flatrentalapi.entity.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

}
