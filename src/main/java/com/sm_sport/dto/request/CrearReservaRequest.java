package com.sm_sport.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearReservaRequest {

    @NotBlank(message = "El ID del servicio es obligatorio")
    private String idServicio;

    @NotNull(message = "La fecha de reserva es obligatoria")
    @Future(message = "La fecha de reserva debe ser futura")
    private LocalDate fechaReserva;

    @NotNull(message = "La hora de reserva es obligatoria")
    private LocalTime horaReserva;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notasCliente;
}
