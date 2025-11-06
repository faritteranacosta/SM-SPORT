package com.sm_sport.mapper;

import com.sm_sport.dto.request.CrearResenaRequest;
import com.sm_sport.dto.response.ResenaResponse;
import com.sm_sport.model.entity.Resena;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResenaMapper {

    // Resena a ResenaResponse
    @Mapping(source = "cliente.idUsuario", target = "idCliente")
    @Mapping(source = "cliente.nombre", target = "nombreCliente")
    @Mapping(source = "servicio.idServicio", target = "idServicio")
    @Mapping(source = "servicio.nombre", target = "nombreServicio")
    ResenaResponse toResponse(Resena resena);

    List<ResenaResponse> toResponseList(List<Resena> resenas);

    // CrearResenaRequest a Resena
    @Mapping(target = "idResena", ignore = true)
    @Mapping(target = "servicio", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "reserva", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "respuestaProveedor", ignore = true)
    @Mapping(target = "fechaRespuesta", ignore = true)
    @Mapping(target = "reportada", constant = "false")
    @Mapping(target = "estadoRevision", constant = "PUBLICADA")
    Resena toEntity(CrearResenaRequest request);
}
