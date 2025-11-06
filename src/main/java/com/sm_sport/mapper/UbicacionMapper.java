package com.sm_sport.mapper;

import com.sm_sport.dto.request.UbicacionRequest;
import com.sm_sport.dto.response.UbicacionResponse;
import com.sm_sport.model.entity.UbicacionServicio;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UbicacionMapper {

    // UbicacionServicio a UbicacionResponse
    UbicacionResponse toResponse(UbicacionServicio ubicacion);

    // UbicacionRequest a UbicacionServicio
    @Mapping(target = "idUbicacion", ignore = true)
    @Mapping(target = "servicio", ignore = true)
    UbicacionServicio toEntity(UbicacionRequest request);

    // Actualizar ubicación
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "idUbicacion", ignore = true)
    @Mapping(target = "servicio", ignore = true)
    void updateEntityFromRequest(UbicacionRequest request, @MappingTarget UbicacionServicio ubicacion);
}
