package com.sm_sport.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearServicioRequest {

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(min = 5, max = 150, message = "El nombre debe tener entre 5 y 150 caracteres")
    private String nombre;

    @NotBlank(message = "El deporte es obligatorio")
    @Size(max = 50, message = "El deporte no puede exceder 50 caracteres")
    private String deporte;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "9999999.99", message = "El precio no puede exceder 9,999,999.99")
    @Digits(integer = 7, fraction = 2, message = "El precio debe tener máximo 7 dígitos enteros y 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "La ubicación es obligatoria")
    @Valid
    private UbicacionRequest ubicacion;

    @Valid
    private List<DisponibilidadRequest> disponibilidad;
}
