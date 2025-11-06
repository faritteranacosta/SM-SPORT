package com.sm_sport.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarServicioRequest {

    @Size(min = 5, max = 150, message = "El nombre debe tener entre 5 y 150 caracteres")
    private String nombre;

    @Size(max = 50, message = "El deporte no puede exceder 50 caracteres")
    private String deporte;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "9999999.99", message = "El precio no puede exceder 9,999,999.99")
    private BigDecimal precio;

    @Valid
    private UbicacionRequest ubicacion;
}
