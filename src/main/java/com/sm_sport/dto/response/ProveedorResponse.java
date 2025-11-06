package com.sm_sport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProveedorResponse extends UsuarioResponse {

    private BigDecimal saldoCuenta;
    private BigDecimal calificacionPromedio;
    private Integer totalServiciosPublicados;
    private Integer totalReservasCompletadas;
    private String descripcionNegocio;
    private Boolean verificado;
}
