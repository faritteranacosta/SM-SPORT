package com.sm_sport.dto.response;

import com.sm_sport.model.enums.AccionDenuncia;
import com.sm_sport.model.enums.EstadoDenuncia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DenunciaResponse {

    private String idDenuncia;
    private String tipoDenuncia;
    private String descripcion;
    private String evidencia;
    private LocalDateTime fechaDenuncia;
    private EstadoDenuncia estado;
    private String respuestaAdmin;
    private AccionDenuncia accionTomada;
    private LocalDateTime fechaRespuesta;

    // Usuario denunciante
    private String idUsuarioDenunciante;
    private String nombreDenunciante;

    // Usuario denunciado
    private String idUsuarioDenunciado;
    private String nombreDenunciado;

    // Administrador que atendió
    private String idAdministrador;
    private String nombreAdministrador;
}
