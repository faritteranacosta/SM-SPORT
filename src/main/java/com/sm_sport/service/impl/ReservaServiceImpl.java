package com.sm_sport.service.impl;

import com.sm_sport.dto.request.CancelarReservaRequest;
import com.sm_sport.dto.request.CrearReservaRequest;
import com.sm_sport.dto.request.FiltroReservaRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PageResponse;
import com.sm_sport.dto.response.ReservaDetalleResponse;
import com.sm_sport.dto.response.ReservaResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ForbiddenException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.PageMapper;
import com.sm_sport.mapper.ReservaMapper;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.model.enums.EstadoServicio;
import com.sm_sport.repository.ClienteRepository;
import com.sm_sport.repository.DisponibilidadServicioRepository;
import com.sm_sport.repository.ReservaRepository;
import com.sm_sport.repository.ServicioRepository;
//import com.sm_sport.service.NotificacionService;
import com.sm_sport.service.ReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final ServicioRepository servicioRepository;
    private final DisponibilidadServicioRepository disponibilidadRepository;
    private final ReservaMapper reservaMapper;
    private final PageMapper pageMapper;
    //private final NotificacionService notificacionService;

    @Override
    public ReservaResponse crearReserva(String idCliente, CrearReservaRequest request) {
        log.info("Creando reserva para cliente: {}", idCliente);

        // Verificar cliente
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // Verificar servicio
        Servicio servicio = servicioRepository.findById(request.getIdServicio())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // Validar que el servicio esté publicado
        if (servicio.getEstado() != EstadoServicio.PUBLICADO) {
            throw new BusinessException("El servicio no está disponible");
        }

        // Verificar disponibilidad
        if (!verificarDisponibilidad(request.getIdServicio(), request)) {
            throw new BusinessException("No hay disponibilidad para la fecha y hora seleccionadas");
        }

        // Crear reserva
        Reserva reserva = reservaMapper.toEntity(request);
        reserva.setCliente(cliente);
        reserva.setServicio(servicio);
        reserva.setProveedor(servicio.getProveedor());
        reserva.setCostoTotal(servicio.getPrecio());
        reserva.setEstado(EstadoReserva.PENDIENTE);

        reserva = reservaRepository.save(reserva);

        // Reducir cupos disponibles
        DisponibilidadServicio disponibilidad = disponibilidadRepository
                .findByServicioAndFecha(request.getIdServicio(), request.getFechaReserva())
                .stream()
                .filter(d -> request.getHoraReserva().isAfter(d.getHoraInicio())
                        && request.getHoraReserva().isBefore(d.getHoraFin()))
                .findFirst()
                .orElse(null);

        if (disponibilidad != null && disponibilidad.getCuposDisponibles() > 0) {
            disponibilidad.setCuposDisponibles(disponibilidad.getCuposDisponibles() - 1);
            if (disponibilidad.getCuposDisponibles() == 0) {
                disponibilidad.setDisponible(false);
            }
            disponibilidadRepository.save(disponibilidad);
        }

        // Notificar al proveedor
//        notificacionService.enviarNotificacion(
//                servicio.getProveedor().getIdUsuario(),
//                "RESERVA",
//                "Nueva reserva recibida",
//                String.format("Has recibido una nueva reserva para %s", servicio.getNombre())
//        );

        log.info("Reserva creada exitosamente: {}", reserva.getIdReserva());

        return reservaMapper.toResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaResponse obtenerPorId(String idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        return reservaMapper.toResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaDetalleResponse obtenerDetalle(String idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        return reservaMapper.toDetalleResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservaResponse> listarPorCliente(String idCliente, Integer pagina, Integer tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("fechaReserva").descending());

        Page<Reserva> reservas = reservaRepository.findByClienteIdUsuario(idCliente, pageable);
        Page<ReservaResponse> reservasResponse = reservas.map(reservaMapper::toResponse);

        return pageMapper.toPageResponse(reservasResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservaResponse> listarPorProveedor(String idProveedor, Integer pagina, Integer tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("fechaCreacion").descending());

        Page<Reserva> reservas = reservaRepository.findByProveedorIdUsuario(idProveedor, pageable);
        Page<ReservaResponse> reservasResponse = reservas.map(reservaMapper::toResponse);

        return pageMapper.toPageResponse(reservasResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservaResponse> filtrarReservas(FiltroReservaRequest filtros) {
        Pageable pageable = PageRequest.of(
                filtros.getPagina(),
                filtros.getTamano(),
                Sort.by(Sort.Direction.fromString(filtros.getDireccion()), filtros.getOrdenarPor())
        );

        // TODO: Implementar filtros dinámicos con Specifications
        Page<Reserva> reservas = reservaRepository.findAll(pageable);
        Page<ReservaResponse> reservasResponse = reservas.map(reservaMapper::toResponse);

        return pageMapper.toPageResponse(reservasResponse);
    }

    @Override
    public ReservaResponse confirmarReserva(String idReserva, String idProveedor) {
        log.info("Confirmando reserva: {} por proveedor: {}", idReserva, idProveedor);

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Validar que el proveedor sea el dueño del servicio
        if (!reserva.getProveedor().getIdUsuario().equals(idProveedor)) {
            throw new ForbiddenException("No tienes permiso para confirmar esta reserva");
        }

        // Validar estado
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new BusinessException("Solo se pueden confirmar reservas pendientes");
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva = reservaRepository.save(reserva);

        // Notificar al cliente
//        notificacionService.enviarNotificacion(
//                reserva.getCliente().getIdUsuario(),
//                "RESERVA",
//                "Reserva confirmada",
//                "Tu reserva ha sido confirmada por el proveedor"
//        );

        log.info("Reserva confirmada exitosamente: {}", idReserva);

        return reservaMapper.toResponse(reserva);
    }

    @Override
    public ReservaResponse rechazarReserva(String idReserva, String idProveedor, String motivo) {
        log.info("Rechazando reserva: {}", idReserva);

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Validar que el proveedor sea el dueño del servicio
        if (!reserva.getProveedor().getIdUsuario().equals(idProveedor)) {
            throw new ForbiddenException("No tienes permiso para rechazar esta reserva");
        }

        reserva.setEstado(EstadoReserva.RECHAZADA);
        reserva = reservaRepository.save(reserva);

        // Restaurar disponibilidad
        // TODO: Implementar lógica de restauración de cupos

        // Notificar al cliente
//        notificacionService.enviarNotificacion(
//                reserva.getCliente().getIdUsuario(),
//                "CANCELACION",
//                "Reserva rechazada",
//                String.format("Tu reserva ha sido rechazada. Motivo: %s", motivo)
//        );

        return reservaMapper.toResponse(reserva);
    }

    @Override
    public MessageResponse cancelarReserva(String idReserva, String idCliente, CancelarReservaRequest request) {
        log.info("Cancelando reserva: {}", idReserva);

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Validar que el cliente sea el dueño
        if (!reserva.getCliente().getIdUsuario().equals(idCliente)) {
            throw new ForbiddenException("No tienes permiso para cancelar esta reserva");
        }

        // Validar que se pueda cancelar
        if (reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new BusinessException("No se puede cancelar una reserva finalizada");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setNotasCliente(request.getMotivoCancelacion());
        reservaRepository.save(reserva);

        // Notificar al proveedor
//        notificacionService.enviarNotificacion(
//                reserva.getProveedor().getIdUsuario(),
//                "CANCELACION",
//                "Reserva cancelada",
//                String.format("El cliente ha cancelado una reserva. Motivo: %s", request.getMotivoCancelacion())
//        );

        log.info("Reserva cancelada exitosamente: {}", idReserva);

        return MessageResponse.success("Reserva cancelada exitosamente");
    }

    @Override
    public ReservaResponse finalizarReserva(String idReserva) {
        log.info("Finalizando reserva: {}", idReserva);

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        reserva.setEstado(EstadoReserva.FINALIZADA);
        reserva = reservaRepository.save(reserva);

        // Actualizar contador del proveedor
        Proveedor proveedor = reserva.getProveedor();
        proveedor.setTotalReservasCompletadas(proveedor.getTotalReservasCompletadas() + 1);

        // Notificar al cliente para que pueda calificar
//        notificacionService.enviarNotificacion(
//                reserva.getCliente().getIdUsuario(),
//                "RESERVA",
//                "Reserva finalizada",
//                "Tu reserva ha finalizado. ¡Califica el servicio!"
//        );

        return reservaMapper.toResponse(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(String idServicio, CrearReservaRequest request) {
        return disponibilidadRepository.verificarDisponibilidad(
                idServicio,
                request.getFechaReserva(),
                request.getHoraReserva()
        );
    }

    @Override
    public Integer cancelarReservasExpiradas() {
        log.info("Cancelando reservas pendientes expiradas");

        // Obtener tiempo de expiración (48 horas por defecto)
        LocalDateTime fechaLimite = LocalDateTime.now().minusHours(48);

        List<Reserva> reservasExpiradas = reservaRepository.findReservasPendientesAntiguas(fechaLimite);

        reservasExpiradas.forEach(reserva -> {
            reserva.setEstado(EstadoReserva.CANCELADA);

            // Notificar al cliente
//            notificacionService.enviarNotificacion(
//                    reserva.getCliente().getIdUsuario(),
//                    "CANCELACION",
//                    "Reserva cancelada por expiración",
//                    "Tu reserva ha sido cancelada automáticamente por falta de pago"
//            );
        });

        reservaRepository.saveAll(reservasExpiradas);

        log.info("Reservas canceladas: {}", reservasExpiradas.size());

        return reservasExpiradas.size();
    }
}
