package com.komy.flatrentalapi.dto.reservation;

import com.komy.flatrentalapi.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "apartment.id", target = "apartmentId")
    @Mapping(source = "status", target = "status")
    ReservationResponse toResponse(Reservation reservation);
}
