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
public class ComprobanteResponse {

    private String idComprobante;
    private String idPago;
    private LocalDateTime fechaEmision;
    private String detalle;
    private BigDecimal monto;
    private String formato;
    private String urlArchivo;
}
