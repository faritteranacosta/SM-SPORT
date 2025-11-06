package com.sm_sport.dto.response;

import com.sm_sport.model.enums.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDetalleResponse {

    private String idReserva;
    private LocalDate fechaReserva;
    private LocalTime horaReserva;
    private EstadoReserva estado;
    private LocalDateTime fechaCreacion;
    private BigDecimal costoTotal;
    private String notasCliente;

    // Cliente completo
    private ClienteResponse cliente;

    // Servicio completo
    private ServicioDetalleResponse servicio;

    // Pago asociado
    private PagoResponse pago;

    // Reseña asociada
    private ResenaResponse resena;

    // Solicitud de reembolso
    private SolicitudReembolsoResponse solicitudReembolso;
}
