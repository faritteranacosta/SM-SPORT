package com.sm_sport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReembolsoRequest {

    @NotBlank(message = "El ID de la reserva es obligatorio")
    private String idReserva;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(min = 20, max = 500, message = "El motivo debe tener entre 20 y 500 caracteres")
    private String motivoCancelacion;
}
