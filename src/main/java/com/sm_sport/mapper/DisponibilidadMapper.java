package com.sm_sport.mapper;

import com.sm_sport.dto.request.DisponibilidadRequest;
import com.sm_sport.dto.response.DisponibilidadResponse;
import com.sm_sport.model.entity.DisponibilidadServicio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DisponibilidadMapper {

    // DisponibilidadServicio a DisponibilidadResponse
    DisponibilidadResponse toResponse(DisponibilidadServicio disponibilidad);

    List<DisponibilidadResponse> toResponseList(List<DisponibilidadServicio> disponibilidades);

    // DisponibilidadRequest a DisponibilidadServicio
    @Mapping(target = "idDisponibilidad", ignore = true)
    @Mapping(target = "servicio", ignore = true)
    @Mapping(target = "disponible", constant = "true")
    DisponibilidadServicio toEntity(DisponibilidadRequest request);

    List<DisponibilidadServicio> toEntityList(List<DisponibilidadRequest> requests);
}
