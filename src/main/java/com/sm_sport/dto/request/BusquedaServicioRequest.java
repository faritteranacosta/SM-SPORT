package com.sm_sport.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusquedaServicioRequest {

    private String deporte;
    private String ciudad;
    private String departamento;
    private BigDecimal precioMin;
    private BigDecimal precioMax;
    private BigDecimal calificacionMinima;
    private String texto; // Búsqueda en nombre o descripción

    // Para búsqueda por proximidad
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer radioKm;

    // Paginación
    private Integer pagina = 0;
    private Integer tamano = 20;
    private String ordenarPor = "fechaPublicacion";
    private String direccion = "DESC";
}
