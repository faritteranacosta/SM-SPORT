package com.sm_sport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearDenunciaRequest {

    @NotBlank(message = "El ID del usuario denunciado es obligatorio")
    private String idUsuarioDenunciado;

    @NotBlank(message = "El tipo de denuncia es obligatorio")
    @Pattern(
            regexp = "SERVICIO_FRAUDULENTO|COMPORTAMIENTO_INAPROPIADO|INCUMPLIMIENTO|OTRO",
            message = "Tipo de denuncia inválido"
    )
    private String tipoDenuncia;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 20, max = 1000, message = "La descripción debe tener entre 20 y 1000 caracteres")
    private String descripcion;

    @Size(max = 500, message = "La evidencia no puede exceder 500 caracteres")
    private String evidencia;
}
