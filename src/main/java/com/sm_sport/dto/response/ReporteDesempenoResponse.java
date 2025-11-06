package com.sm_sport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteDesempenoResponse {

    private String idReporte;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime fechaGeneracion;
    private Integer totalVentas;
    private Integer reservasCanceladas;
    private BigDecimal ingresosGenerados;
    private BigDecimal calificacionPromedio;
    private Integer totalResenas;

    // Información del proveedor
    private String idProveedor;
    private String nombreProveedor;
}
