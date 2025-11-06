package com.sm_sport.dto.request;

import com.sm_sport.model.enums.EstadoUsuario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEstadoUsuarioRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoUsuario estado;

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;
}
