package com.sm_sport.service.impl;

import com.sm_sport.dto.request.ReembolsoRequest;
import com.sm_sport.dto.response.SolicitudReembolsoResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.ReembolsoMapper;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.EstadoPago;
import com.sm_sport.model.enums.EstadoReembolso;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.repository.*;
import com.sm_sport.service.EmailService;
import com.sm_sport.service.NotificacionService;
import com.sm_sport.service.ReembolsoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReembolsoServiceImpl implements ReembolsoService {

    private final SolicitudReembolsoRepository solicitudRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final ReembolsoMapper reembolsoMapper;
    private final NotificacionService notificacionService;
    private final EmailService emailService;

    // Políticas de reembolso por defecto
    private static final int DIAS_REEMBOLSO_COMPLETO = 7;
    private static final BigDecimal PORCENTAJE_7_DIAS = BigDecimal.valueOf(100);
    private static final BigDecimal PORCENTAJE_3_6_DIAS = BigDecimal.valueOf(90);
    private static final BigDecimal PORCENTAJE_MENOS_3_DIAS = BigDecimal.valueOf(80);

    @Override
    @Transactional
    public SolicitudReembolsoResponse solicitarReembolso(String idCliente, ReembolsoRequest request) {
        log.info("Cliente {} solicitando reembolso para reserva {}", idCliente, request.getIdReserva());

        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con ID: " + idCliente));

        // Validar que la reserva existe
        Reserva reserva = reservaRepository.findById(request.getIdReserva())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reserva no encontrada con ID: " + request.getIdReserva()));

        // Validar que la reserva pertenece al cliente
        if (!reserva.getCliente().getIdUsuario().equals(idCliente)) {
            throw new BusinessException("La reserva no pertenece al cliente especificado");
        }

        // Validar que la reserva esté confirmada o pendiente
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA &&
                reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new BusinessException(
                    "Solo se pueden solicitar reembolsos para reservas confirmadas o pendientes");
        }

        // Validar que la reserva esté pagada
        Pago pago = pagoRepository.findByReservaIdReserva(request.getIdReserva())
                .orElseThrow(() -> new BusinessException(
                        "No se encontró un pago asociado a esta reserva"));

        if (pago.getEstadoPago() != EstadoPago.APROBADO) {
            throw new BusinessException("El pago de la reserva no está aprobado");
        }

        // Validar que no exista ya una solicitud de reembolso
        if (solicitudRepository.findByReservaIdReserva(request.getIdReserva()).isPresent()) {
            throw new BusinessException("Ya existe una solicitud de reembolso para esta reserva");
        }

        // Calcular monto de reembolso según políticas
        BigDecimal montoReembolso = calcularMontoReembolso(request.getIdReserva());

        // Crear solicitud de reembolso
        SolicitudReembolso solicitud = reembolsoMapper.toEntity(request);
        solicitud.setReserva(reserva);
        solicitud.setCliente(cliente);
        solicitud.setMontoReembolso(montoReembolso);
        solicitud.setEstado(EstadoReembolso.SOLICITADO);
        solicitud.setFechaSolicitud(LocalDateTime.now());

        // Guardar
        SolicitudReembolso solicitudGuardada = solicitudRepository.save(solicitud);

        // Actualizar estado de la reserva a CANCELADA
        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepository.save(reserva);

        // Notificar al cliente
        notificarSolicitudCreada(cliente, reserva, montoReembolso);

        log.info("Solicitud de reembolso creada exitosamente: {}", solicitudGuardada.getIdSolicitud());

        return reembolsoMapper.toResponse(solicitudGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudReembolsoResponse obtenerPorId(String idSolicitud) {
        log.info("Obteniendo solicitud de reembolso: {}", idSolicitud);

        SolicitudReembolso solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitud de reembolso no encontrada con ID: " + idSolicitud));

        return reembolsoMapper.toResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudReembolsoResponse> listarPorCliente(String idCliente) {
        log.info("Listando solicitudes de reembolso del cliente: {}", idCliente);

        // Validar que el cliente existe
        if (!clienteRepository.existsById(idCliente)) {
            throw new ResourceNotFoundException("Cliente no encontrado con ID: " + idCliente);
        }

        List<SolicitudReembolso> solicitudes = solicitudRepository
                .findByClienteIdUsuario(idCliente);

        log.info("Se encontraron {} solicitudes para el cliente {}",
                solicitudes.size(), idCliente);

        return reembolsoMapper.toResponseList(solicitudes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudReembolsoResponse> listarPendientes() {
        log.info("Listando solicitudes de reembolso pendientes");

        List<SolicitudReembolso> solicitudes = solicitudRepository.findSolicitudesPendientes();

        log.info("Se encontraron {} solicitudes pendientes", solicitudes.size());

        return reembolsoMapper.toResponseList(solicitudes);
    }

    @Override
    @Transactional
    public SolicitudReembolsoResponse aprobarReembolso(String idSolicitud, String idAdmin) {
        log.info("Administrador {} aprobando solicitud de reembolso {}", idAdmin, idSolicitud);

        // Validar que el administrador existe
        Administrador admin = administradorRepository.findById(idAdmin)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Administrador no encontrado con ID: " + idAdmin));

        // Validar que la solicitud existe
        SolicitudReembolso solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitud de reembolso no encontrada con ID: " + idSolicitud));

        // Validar que la solicitud esté en estado SOLICITADO o EN_REVISION
        if (solicitud.getEstado() == EstadoReembolso.APROBADO) {
            throw new BusinessException("Esta solicitud ya fue aprobada previamente");
        }

        if (solicitud.getEstado() == EstadoReembolso.RECHAZADO) {
            throw new BusinessException("Esta solicitud fue rechazada y no puede ser aprobada");
        }

        // Actualizar estado de la solicitud
        solicitud.setEstado(EstadoReembolso.APROBADO);
        solicitud.setFechaAprobacion(LocalDateTime.now());
        solicitud.setObservacionesAdmin("Reembolso aprobado por el administrador");

        // Actualizar estado del pago a REEMBOLSADO
        Pago pago = pagoRepository.findByReservaIdReserva(solicitud.getReserva().getIdReserva())
                .orElseThrow(() -> new BusinessException("No se encontró el pago asociado"));

        pago.setEstadoPago(EstadoPago.REEMBOLSADO);
        pagoRepository.save(pago);

        // Guardar solicitud actualizada
        SolicitudReembolso solicitudActualizada = solicitudRepository.save(solicitud);

        // Notificar al cliente
        notificarReembolsoAprobado(solicitud);

        log.info("Solicitud de reembolso {} aprobada exitosamente", idSolicitud);

        return reembolsoMapper.toResponse(solicitudActualizada);
    }

    @Override
    @Transactional
    public SolicitudReembolsoResponse rechazarReembolso(String idSolicitud, String idAdmin,
                                                        String motivo) {
        log.info("Administrador {} rechazando solicitud de reembolso {}", idAdmin, idSolicitud);

        // Validar que el administrador existe
        Administrador admin = administradorRepository.findById(idAdmin)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Administrador no encontrado con ID: " + idAdmin));

        // Validar que la solicitud existe
        SolicitudReembolso solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Solicitud de reembolso no encontrada con ID: " + idSolicitud));

        // Validar que la solicitud no esté ya procesada
        if (solicitud.getEstado() == EstadoReembolso.APROBADO ||
                solicitud.getEstado() == EstadoReembolso.RECHAZADO) {
            throw new BusinessException("Esta solicitud ya fue procesada previamente");
        }

        // Validar que se proporcione un motivo
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new BusinessException("Debe proporcionar un motivo para el rechazo");
        }

        // Actualizar estado de la solicitud
        solicitud.setEstado(EstadoReembolso.RECHAZADO);
        solicitud.setFechaAprobacion(LocalDateTime.now());
        solicitud.setObservacionesAdmin(motivo);

        // Revertir estado de la reserva a CONFIRMADA (ya que no se hará reembolso)
        Reserva reserva = solicitud.getReserva();
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reservaRepository.save(reserva);

        // Guardar solicitud actualizada
        SolicitudReembolso solicitudActualizada = solicitudRepository.save(solicitud);

        // Notificar al cliente
        notificarReembolsoRechazado(solicitud, motivo);

        log.info("Solicitud de reembolso {} rechazada", idSolicitud);

        return reembolsoMapper.toResponse(solicitudActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularMontoReembolso(String idReserva) {
        log.info("Calculando monto de reembolso para reserva: {}", idReserva);

        // Obtener la reserva
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reserva no encontrada con ID: " + idReserva));

        // Obtener el pago
        Pago pago = pagoRepository.findByReservaIdReserva(idReserva)
                .orElseThrow(() -> new BusinessException(
                        "No se encontró un pago asociado a esta reserva"));

        BigDecimal montoOriginal = pago.getMonto();

        // Calcular días de anticipación
        LocalDate fechaReserva = reserva.getFechaReserva();
        LocalDate fechaActual = LocalDate.now();
        long diasAnticipacion = ChronoUnit.DAYS.between(fechaActual, fechaReserva);

        log.info("Días de anticipación para cancelación: {}", diasAnticipacion);

        // Aplicar política de reembolso
        BigDecimal porcentajeReembolso;

        if (diasAnticipacion >= DIAS_REEMBOLSO_COMPLETO) {
            // 7+ días antes = 100%
            porcentajeReembolso = PORCENTAJE_7_DIAS;
        } else if (diasAnticipacion >= 3 && diasAnticipacion < DIAS_REEMBOLSO_COMPLETO) {
            // 3-6 días antes = 90%
            porcentajeReembolso = PORCENTAJE_3_6_DIAS;
        } else {
            // Menos de 3 días = 80%
            porcentajeReembolso = PORCENTAJE_MENOS_3_DIAS;
        }

        // Calcular monto de reembolso
        BigDecimal montoReembolso = montoOriginal
                .multiply(porcentajeReembolso)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        log.info("Monto original: {}, Porcentaje: {}%, Monto a reembolsar: {}",
                montoOriginal, porcentajeReembolso, montoReembolso);

        return montoReembolso;
    }

    // ==================== MÉTODOS PRIVADOS DE NOTIFICACIÓN ====================

    private void notificarSolicitudCreada(Cliente cliente, Reserva reserva,
                                          BigDecimal montoReembolso) {
        String nombreServicio = reserva.getServicio().getNombre();
        String montoFormateado = formatearPrecio(montoReembolso);

        // Notificación en app
        notificacionService.notificarSistema(
                cliente.getIdUsuario(),
                "Solicitud de Reembolso Recibida",
                String.format("Tu solicitud de reembolso de %s para el servicio '%s' " +
                                "ha sido recibida y está siendo revisada. Te notificaremos cuando sea procesada.",
                        montoFormateado, nombreServicio)
        );

        // Email
        emailService.enviarEmailCancelacion(
                cliente.getCorreo(),
                reserva.getIdReserva()
        );

        log.info("Notificaciones de solicitud creada enviadas al cliente {}",
                cliente.getIdUsuario());
    }

    private void notificarReembolsoAprobado(SolicitudReembolso solicitud) {
        Cliente cliente = solicitud.getCliente();
        String nombreServicio = solicitud.getReserva().getServicio().getNombre();
        String montoFormateado = formatearPrecio(solicitud.getMontoReembolso());

        // Notificación en app
        notificacionService.notificarSistema(
                cliente.getIdUsuario(),
                "Reembolso Aprobado",
                String.format("Tu reembolso de %s para el servicio '%s' ha sido aprobado. " +
                                "El monto será devuelto a tu método de pago original en 3-5 días hábiles.",
                        montoFormateado, nombreServicio)
        );

        log.info("Notificación de reembolso aprobado enviada al cliente {}",
                cliente.getIdUsuario());
    }

    private void notificarReembolsoRechazado(SolicitudReembolso solicitud, String motivo) {
        Cliente cliente = solicitud.getCliente();
        String nombreServicio = solicitud.getReserva().getServicio().getNombre();

        // Notificación en app
        notificacionService.notificarSistema(
                cliente.getIdUsuario(),
                "Solicitud de Reembolso Rechazada",
                String.format("Tu solicitud de reembolso para el servicio '%s' ha sido rechazada. " +
                                "Motivo: %s. Si tienes dudas, contacta con soporte.",
                        nombreServicio, motivo)
        );

        log.info("Notificación de reembolso rechazado enviada al cliente {}",
                cliente.getIdUsuario());
    }

    // ==================== HELPERS ====================

    private String formatearPrecio(BigDecimal precio) {
        return String.format("$%,.0f COP", precio);
    }
}