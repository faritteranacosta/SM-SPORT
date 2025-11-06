package com.sm_sport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponderResenaRequest {

    @NotBlank(message = "La respuesta es obligatoria")
    @Size(min = 10, max = 500, message = "La respuesta debe tener entre 10 y 500 caracteres")
    private String respuesta;
}
