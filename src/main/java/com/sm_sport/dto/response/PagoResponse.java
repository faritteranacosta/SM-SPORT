package com.sm_sport.dto.response;

import com.sm_sport.model.enums.EstadoPago;
import com.sm_sport.model.enums.MetodoPago;
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
public class PagoResponse {

    private String idPago;
    private String idReserva;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private String referenciaPago;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaAprobacion;
    private String proveedorPasarela;
}
