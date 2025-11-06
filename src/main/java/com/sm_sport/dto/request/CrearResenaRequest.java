package com.sm_sport.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearResenaRequest {

    @NotBlank(message = "El ID de la reserva es obligatorio")
    private String idReserva;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer calificacion;

    @Size(min = 10, max = 1000, message = "El comentario debe tener entre 10 y 1000 caracteres")
    private String comentario;
}
