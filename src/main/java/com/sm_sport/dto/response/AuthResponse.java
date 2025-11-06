package com.sm_sport.dto.response;

import com.sm_sport.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String tipo = "Bearer";
    private Long expiresIn; // milisegundos

    // Información del usuario
    private String idUsuario;
    private String nombre;
    private String correo;
    private Rol rol;
}
