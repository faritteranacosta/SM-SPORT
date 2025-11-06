package com.sm_sport.dto.response;

import com.sm_sport.model.enums.EstadoUsuario;
import com.sm_sport.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UsuarioResponse {

    private String idUsuario;
    private String nombre;
    private String correo;
    private String telefono;
    private String direccion;
    private Rol rol;
    private EstadoUsuario estado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
