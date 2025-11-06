package com.sm_sport.mapper;

import com.sm_sport.dto.request.PagoRequest;
import com.sm_sport.dto.response.ComprobanteResponse;
import com.sm_sport.dto.response.PagoResponse;
import com.sm_sport.model.entity.Comprobante;
import com.sm_sport.model.entity.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PagoMapper {

    // Pago a PagoResponse
    @Mapping(source = "reserva.idReserva", target = "idReserva")
    PagoResponse toResponse(Pago pago);

    List<PagoResponse> toResponseList(List<Pago> pagos);

    // Comprobante a ComprobanteResponse
    @Mapping(source = "pago.idPago", target = "idPago")
    ComprobanteResponse toComprobanteResponse(Comprobante comprobante);

    // PagoRequest a Pago
    @Mapping(target = "idPago", ignore = true)
    @Mapping(target = "reserva", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "monto", ignore = true)
    @Mapping(target = "estadoPago", constant = "PENDIENTE")
    @Mapping(target = "referenciaPago", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "fechaAprobacion", ignore = true)
    @Mapping(target = "proveedorPasarela", ignore = true)
    @Mapping(target = "tokenTransaccion", ignore = true)
    @Mapping(target = "comprobante", ignore = true)
    Pago toEntity(PagoRequest request);
}
