package com.sm_sport.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarReservaRequest {

    @Future(message = "La fecha de reserva debe ser futura")
    private LocalDate fechaReserva;

    private LocalTime horaReserva;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notasCliente;
}
