package com.sm_sport.dto.response;

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
public class MetricaResponse {

    private String idMetrica;
    private String nombreMetrica;
    private BigDecimal valorMetrica;
    private String unidad;
    private LocalDateTime fechaMedicion;
    private String periodo;
    private String categoria;
}
