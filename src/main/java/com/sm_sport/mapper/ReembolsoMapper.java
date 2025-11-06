package com.sm_sport.mapper;

import com.sm_sport.dto.request.ReembolsoRequest;
import com.sm_sport.dto.response.SolicitudReembolsoResponse;
import com.sm_sport.model.entity.SolicitudReembolso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReembolsoMapper {

    // SolicitudReembolso a SolicitudReembolsoResponse
    @Mapping(source = "reserva.idReserva", target = "idReserva")
    @Mapping(source = "cliente.idUsuario", target = "idCliente")
    @Mapping(source = "cliente.nombre", target = "nombreCliente")
    SolicitudReembolsoResponse toResponse(SolicitudReembolso solicitud);

    List<SolicitudReembolsoResponse> toResponseList(List<SolicitudReembolso> solicitudes);

    // ReembolsoRequest a SolicitudReembolso
    @Mapping(target = "idSolicitud", ignore = true)
    @Mapping(target = "reserva", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "montoReembolso", ignore = true)
    @Mapping(target = "fechaSolicitud", ignore = true)
    @Mapping(target = "estado", constant = "SOLICITADO")
    @Mapping(target = "fechaAprobacion", ignore = true)
    @Mapping(target = "observacionesAdmin", ignore = true)
    SolicitudReembolso toEntity(ReembolsoRequest request);
}
