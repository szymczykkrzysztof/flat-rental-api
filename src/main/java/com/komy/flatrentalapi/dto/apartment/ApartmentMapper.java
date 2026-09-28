package com.komy.flatrentalapi.dto.apartment;

import com.komy.flatrentalapi.entity.Apartment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ApartmentMapper {
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "status", target = "status")
    ApartmentResponse toResponse(Apartment apartment);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    Apartment toEntity(ApartmentCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ApartmentUpdateRequest request, @MappingTarget Apartment apartment);

}
