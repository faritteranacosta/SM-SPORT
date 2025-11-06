package com.sm_sport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionResponse {

    private String idUbicacion;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;
    private BigDecimal coordenadasLat;
    private BigDecimal coordenadasLng;
    private Double distanciaKm; // Calculada dinámicamente en búsquedas
}
