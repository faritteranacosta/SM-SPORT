package com.sm_sport.service;

import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.NotificacionResponse;

import java.util.List;

public interface NotificacionService {

    /**
     * Envía una notificación a un usuario
     *
     * @param idUsuario ID del usuario
     * @param tipo      Tipo de notificación
     * @param titulo    Título
     * @param mensaje   Mensaje
     */
    void enviarNotificacion(String idUsuario, String tipo, String titulo, String mensaje);

    /**
     * Lista notificaciones de un usuario
     *
     * @param idUsuario ID del usuario
     * @return Lista de notificaciones
     */
    List<NotificacionResponse> listarPorUsuario(String idUsuario);

    /**
     * Lista notificaciones no leídas
     *
     * @param idUsuario ID del usuario
     * @return Lista de notificaciones no leídas
     */
    List<NotificacionResponse> listarNoLeidas(String idUsuario);

    /**
     * Marca una notificación como leída
     *
     * @param idNotificacion ID de la notificación
     * @param idUsuario      ID del usuario
     * @return Mensaje de confirmación
     */
    MessageResponse marcarComoLeida(String idNotificacion, String idUsuario);

    /**
     * Marca todas las notificaciones como leídas
     *
     * @param idUsuario ID del usuario
     * @return Mensaje de confirmación
     */
    MessageResponse marcarTodasComoLeidas(String idUsuario);

    /**
     * Elimina notificaciones antiguas
     *
     * @param diasAntiguedad Días de antigüedad
     * @return Cantidad de notificaciones eliminadas
     */
    Integer eliminarNotificacionesAntiguas(Integer diasAntiguedad);
}
