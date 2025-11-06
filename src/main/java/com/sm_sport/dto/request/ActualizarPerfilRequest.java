package com.sm_sport.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilRequest {

    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    private String telefono;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    // Específico para Cliente
    private String preferenciaDeportes;
    private String nivelExperiencia;

    // Específico para Proveedor
    private String descripcionNegocio;
}
