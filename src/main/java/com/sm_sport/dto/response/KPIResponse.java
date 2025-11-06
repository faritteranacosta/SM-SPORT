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
public class KPIResponse {

    private String idKpi;
    private String nombreKpi;
    private BigDecimal valorKpi;
    private String periodo;
    private String tendencia;
    private LocalDateTime fechaCalculo;
    private BigDecimal metaObjetivo;
    private BigDecimal porcentajeCumplimiento;
}
