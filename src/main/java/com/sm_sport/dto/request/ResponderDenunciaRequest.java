package com.sm_sport.dto.request;

import com.sm_sport.model.enums.AccionDenuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponderDenunciaRequest {

    @NotBlank(message = "La respuesta es obligatoria")
    @Size(min = 20, max = 1000, message = "La respuesta debe tener entre 20 y 1000 caracteres")
    private String respuesta;

    private AccionDenuncia accionTomada;
}
