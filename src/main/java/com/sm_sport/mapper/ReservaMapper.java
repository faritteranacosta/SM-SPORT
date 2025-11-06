package com.sm_sport.mapper;

import com.sm_sport.dto.request.CrearReservaRequest;
import com.sm_sport.dto.response.ReservaDetalleResponse;
import com.sm_sport.dto.response.ReservaResponse;
import com.sm_sport.model.entity.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {ServicioMapper.class, PagoMapper.class, ResenaMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReservaMapper {

    // Reserva a ReservaResponse
    @Mapping(source = "cliente.idUsuario", target = "idCliente")
    @Mapping(source = "cliente.nombre", target = "nombreCliente")
    @Mapping(source = "servicio.idServicio", target = "idServicio")
    @Mapping(source = "servicio.nombre", target = "nombreServicio")
    @Mapping(source = "servicio.deporte", target = "deporteServicio")
    @Mapping(source = "proveedor.idUsuario", target = "idProveedor")
    @Mapping(source = "proveedor.nombre", target = "nombreProveedor")
    @Mapping(target = "pagada", expression = "java(reserva.getPago() != null && reserva.getPago().getEstadoPago() == com.sm_sport.model.enums.EstadoPago.APROBADO)")
    @Mapping(target = "resenada", expression = "java(reserva.getResena() != null)")
    ReservaResponse toResponse(Reserva reserva);

    List<ReservaResponse> toResponseList(List<Reserva> reservas);

    // Reserva a ReservaDetalleResponse
    @Mapping(source = "cliente", target = "cliente")
    @Mapping(source = "servicio", target = "servicio")
    @Mapping(source = "pago", target = "pago")
    @Mapping(source = "resena", target = "resena")
    @Mapping(source = "solicitudReembolso", target = "solicitudReembolso")
    ReservaDetalleResponse toDetalleResponse(Reserva reserva);

    // CrearReservaRequest a Reserva
    @Mapping(target = "idReserva", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "servicio", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "estado", constant = "PENDIENTE")
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "costoTotal", ignore = true)
    @Mapping(target = "pago", ignore = true)
    @Mapping(target = "resena", ignore = true)
    @Mapping(target = "solicitudReembolso", ignore = true)
    @Mapping(target = "auditoria", ignore = true)
    Reserva toEntity(CrearReservaRequest request);
}
