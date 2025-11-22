package com.sm_sport.service.impl;

import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.NotificacionResponse;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.NotificacionMapper;
import com.sm_sport.model.entity.Notificacion;
import com.sm_sport.model.entity.Usuario;
import com.sm_sport.repository.NotificacionRepository;
import com.sm_sport.repository.UsuarioRepository;
import com.sm_sport.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionMapper notificacionMapper;

    @Override
    @Async
    @Transactional
    public void enviarNotificacion(String idUsuario, String tipo, String titulo, String mensaje) {
        log.info("Enviando notificación a usuario {}: {}", idUsuario, titulo);

        try {
            // Validar que el usuario existe
            Usuario usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado con ID: " + idUsuario));

            // Crear la notificación
            Notificacion notificacion = Notificacion.builder()
                    .usuario(usuario)
                    .tipoNotificacion(tipo)
                    .titulo(titulo)
                    .mensaje(mensaje)
                    .leida(false)
                    .fechaEnvio(LocalDateTime.now())
                    .build();

            // Guardar
            notificacionRepository.save(notificacion);

            log.info("Notificación enviada exitosamente a usuario {}", idUsuario);

        } catch (Exception e) {
            log.error("Error al enviar notificación a usuario {}: {}", idUsuario, e.getMessage());
            // No lanzamos excepción para no afectar el flujo principal
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(String idUsuario) {
        log.info("Listando notificaciones del usuario: {}", idUsuario);

        // Validar que el usuario existe
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario);
        }

        List<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioIdUsuario(idUsuario);

        log.info("Se encontraron {} notificaciones para el usuario {}",
                notificaciones.size(), idUsuario);

        return notificacionMapper.toResponseList(notificaciones);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarNoLeidas(String idUsuario) {
        log.info("Listando notificaciones no leídas del usuario: {}", idUsuario);

        // Validar que el usuario existe
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario);
        }

        List<Notificacion> notificaciones = notificacionRepository
                .findNotificacionesNoLeidas(idUsuario);

        log.info("El usuario {} tiene {} notificaciones no leídas",
                idUsuario, notificaciones.size());

        return notificacionMapper.toResponseList(notificaciones);
    }

    @Override
    @Transactional
    public MessageResponse marcarComoLeida(String idNotificacion, String idUsuario) {
        log.info("Marcando notificación {} como leída por usuario {}",
                idNotificacion, idUsuario);

        // Validar que la notificación existe
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notificación no encontrada con ID: " + idNotificacion));

        // Validar que la notificación pertenece al usuario
        if (!notificacion.getUsuario().getIdUsuario().equals(idUsuario)) {
            log.warn("Usuario {} intentó marcar notificación {} que no le pertenece",
                    idUsuario, idNotificacion);
            throw new ResourceNotFoundException(
                    "La notificación no pertenece al usuario especificado");
        }

        // Verificar si ya está leída
        if (notificacion.getLeida()) {
            log.info("La notificación {} ya estaba marcada como leída", idNotificacion);
            return MessageResponse.success("La notificación ya estaba marcada como leída");
        }

        // Marcar como leída
        notificacion.setLeida(true);
        notificacion.setFechaLectura(LocalDateTime.now());
        notificacionRepository.save(notificacion);

        log.info("Notificación {} marcada como leída exitosamente", idNotificacion);

        return MessageResponse.success("Notificación marcada como leída");
    }

    @Override
    @Transactional
    public MessageResponse marcarTodasComoLeidas(String idUsuario) {
        log.info("Marcando todas las notificaciones como leídas para usuario: {}", idUsuario);

        // Validar que el usuario existe
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario);
        }

        // Contar notificaciones no leídas antes de marcar
        Long noLeidas = notificacionRepository.contarNotificacionesNoLeidas(idUsuario);

        if (noLeidas == 0) {
            log.info("El usuario {} no tiene notificaciones pendientes", idUsuario);
            return MessageResponse.success("No hay notificaciones pendientes por leer");
        }

        // Marcar todas como leídas
        notificacionRepository.marcarTodasComoLeidas(idUsuario, LocalDateTime.now());

        log.info("Se marcaron {} notificaciones como leídas para el usuario {}",
                noLeidas, idUsuario);

        return MessageResponse.success(
                String.format("Se marcaron %d notificaciones como leídas", noLeidas));
    }

    @Override
    @Transactional
    public Integer eliminarNotificacionesAntiguas(Integer diasAntiguedad) {
        log.info("Eliminando notificaciones con más de {} días", diasAntiguedad);

        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasAntiguedad);
            long total = notificacionRepository.count();
            notificacionRepository.eliminarNotificacionesAntiguas(fechaLimite);

            long eliminadas = total - notificacionRepository.count();
            return (int) eliminadas;

        } catch (Exception e) {
            log.error("Error al eliminar notificaciones antiguas: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Envía notificación de nueva reserva al proveedor
     */
    @Async
    public void notificarNuevaReserva(String idProveedor, String nombreServicio,
                                      String nombreCliente) {
        String titulo = "Nueva Reserva Recibida";
        String mensaje = String.format(
                "Has recibido una nueva reserva de %s para el servicio '%s'. " +
                        "Revisa los detalles en la sección de reservas.",
                nombreCliente, nombreServicio
        );
        enviarNotificacion(idProveedor, "RESERVA", titulo, mensaje);
    }

    /**
     * Envía notificación de reserva confirmada al cliente
     */
    @Async
    public void notificarReservaConfirmada(String idCliente, String nombreServicio,
                                           String fecha) {
        String titulo = "Reserva Confirmada";
        String mensaje = String.format(
                "Tu reserva para '%s' el día %s ha sido confirmada. " +
                        "¡Nos vemos pronto!",
                nombreServicio, fecha
        );
        enviarNotificacion(idCliente, "RESERVA", titulo, mensaje);
    }

    /**
     * Envía notificación de pago aprobado
     */
    @Async
    public void notificarPagoAprobado(String idCliente, String montoFormateado,
                                      String nombreServicio) {
        String titulo = "Pago Aprobado";
        String mensaje = String.format(
                "Tu pago de %s para el servicio '%s' ha sido aprobado exitosamente. " +
                        "El comprobante ha sido enviado a tu correo.",
                montoFormateado, nombreServicio
        );
        enviarNotificacion(idCliente, "PAGO", titulo, mensaje);
    }

    /**
     * Envía notificación de cancelación
     */
    @Async
    public void notificarCancelacion(String idUsuario, String nombreServicio,
                                     String motivo) {
        String titulo = "Reserva Cancelada";
        String mensaje = String.format(
                "Tu reserva para '%s' ha sido cancelada. Motivo: %s. " +
                        "Si realizaste un pago, el reembolso será procesado en 3-5 días hábiles.",
                nombreServicio, motivo
        );
        enviarNotificacion(idUsuario, "CANCELACION", titulo, mensaje);
    }

    /**
     * Envía notificación de nueva reseña al proveedor
     */
    @Async
    public void notificarNuevaResena(String idProveedor, String nombreServicio,
                                     Integer calificacion) {
        String titulo = "Nueva Reseña Recibida";
        String mensaje = String.format(
                "Has recibido una nueva reseña de %d estrellas para tu servicio '%s'. " +
                        "Revísala y responde si lo deseas.",
                calificacion, nombreServicio
        );
        enviarNotificacion(idProveedor, "RESENA", titulo, mensaje);
    }

    /**
     * Envía notificación de recordatorio de reserva
     */
    @Async
    public void notificarRecordatorioReserva(String idCliente, String nombreServicio,
                                             String fecha, String hora) {
        String titulo = "Recordatorio de Reserva";
        String mensaje = String.format(
                "Te recordamos que tienes una reserva para '%s' el día %s a las %s. " +
                        "¡No olvides llegar 10 minutos antes!",
                nombreServicio, fecha, hora
        );
        enviarNotificacion(idCliente, "SISTEMA", titulo, mensaje);
    }

    /**
     * Envía notificación general del sistema
     */
    @Async
    public void notificarSistema(String idUsuario, String titulo, String mensaje) {
        enviarNotificacion(idUsuario, "SISTEMA", titulo, mensaje);
    }
}