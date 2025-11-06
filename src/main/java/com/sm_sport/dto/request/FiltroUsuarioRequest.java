package com.sm_sport.dto.request;

import com.sm_sport.model.enums.EstadoUsuario;
import com.sm_sport.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiltroUsuarioRequest {

    private String nombre;
    private String correo;
    private Rol rol;
    private EstadoUsuario estado;
    private LocalDateTime fechaRegistroDesde;
    private LocalDateTime fechaRegistroHasta;

    // Paginación
    private Integer pagina = 0;
    private Integer tamano = 20;
    private String ordenarPor = "fechaRegistro";
    private String direccion = "DESC";
}
