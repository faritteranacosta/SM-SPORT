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
public class ReservaResponse {

    private String idReserva;
    private LocalDate fechaReserva;
    private LocalTime horaReserva;
    private EstadoReserva estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private BigDecimal costoTotal;
    private String notasCliente;

    // Información del cliente
    private String idCliente;
    private String nombreCliente;

    // Información del servicio
    private String idServicio;
    private String nombreServicio;
    private String deporteServicio;

    // Información del proveedor
    private String idProveedor;
    private String nombreProveedor;

    // Estados de pago y reseña
    private Boolean pagada;
    private Boolean resenada;
}
