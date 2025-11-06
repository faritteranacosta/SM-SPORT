package com.sm_sport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResponse {

    private String idNotificacion;
    private String tipoNotificacion;
    private String titulo;
    private String mensaje;
    private Boolean leida;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaLectura;
}
