package com.sm_sport.dto.request;

import com.sm_sport.model.enums.MetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoRequest {

    @NotBlank(message = "El ID de la reserva es obligatorio")
    private String idReserva;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    // Información de tarjeta (se debe encriptar en producción)
    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaExpiracion;
    private String cvv;

    // Para otros métodos de pago
    private String tokenPago; // Token de pasarela
    private String emailBilletera;
}
