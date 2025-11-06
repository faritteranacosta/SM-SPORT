package com.sm_sport.dto.response;

import com.sm_sport.model.enums.EstadoRevision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResenaResponse {

    private String idResena;
    private Integer calificacion;
    private String comentario;
    private LocalDateTime fechaCreacion;
    private String respuestaProveedor;
    private LocalDateTime fechaRespuesta;
    private Boolean reportada;
    private EstadoRevision estadoRevision;

    // Información del cliente
    private String idCliente;
    private String nombreCliente;

    // Información del servicio
    private String idServicio;
    private String nombreServicio;
}
