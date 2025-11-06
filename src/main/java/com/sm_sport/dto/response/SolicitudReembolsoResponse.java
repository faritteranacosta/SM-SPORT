package com.sm_sport.dto.response;

import com.sm_sport.model.enums.EstadoReembolso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudReembolsoResponse {

    private String idSolicitud;
    private String idReserva;
    private BigDecimal montoReembolso;
    private String motivoCancelacion;
    private LocalDateTime fechaSolicitud;
    private EstadoReembolso estado;
    private LocalDateTime fechaAprobacion;
    private String observacionesAdmin;

    // Información del cliente
    private String idCliente;
    private String nombreCliente;
}
