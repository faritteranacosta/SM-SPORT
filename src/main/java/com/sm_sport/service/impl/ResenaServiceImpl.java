package com.sm_sport.service.impl;

import com.sm_sport.dto.request.CrearResenaRequest;
import com.sm_sport.dto.request.ResponderResenaRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.ResenaResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.ResenaMapper;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.model.enums.EstadoRevision;
import com.sm_sport.repository.*;
import com.sm_sport.service.ResenaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResenaServiceImpl implements ResenaService {

    private final ResenaRepository resenaRepository;
    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;
    private final ServicioRepository servicioRepository;
    private final ProveedorRepository proveedorRepository;
    private final ResenaMapper resenaMapper;

    @Override
    @Transactional
    public ResenaResponse crearResena(String idCliente, CrearResenaRequest request) {
        log.info("Creando reseña para cliente: {} en reserva: {}", idCliente, request.getIdReserva());

        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + idCliente));

        // Validar que la reserva existe
        Reserva reserva = reservaRepository.findById(request.getIdReserva())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + request.getIdReserva()));

        // Validar que la reserva pertenece al cliente
        if (!reserva.getCliente().getIdUsuario().equals(idCliente)) {
            throw new BusinessException("La reserva no pertenece al cliente");
        }

        // Validar que la reserva está finalizada
        if (reserva.getEstado() != EstadoReserva.FINALIZADA) {
            throw new BusinessException("Solo se pueden calificar reservas finalizadas");
        }

        // Validar que no exista una reseña previa para esta reserva
        if (resenaRepository.findByReservaIdReserva(request.getIdReserva()).isPresent()) {
            throw new BusinessException("Ya existe una reseña para esta reserva");
        }

        // Crear la reseña
        Resena resena = resenaMapper.toEntity(request);
        resena.setCliente(cliente);
        resena.setServicio(reserva.getServicio());
        resena.setReserva(reserva);

        // Guardar la reseña
        Resena resenaGuardada = resenaRepository.save(resena);

        // Actualizar calificación promedio del servicio
        actualizarCalificacionServicio(reserva.getServicio().getIdServicio());

        log.info("Reseña creada exitosamente con ID: {}", resenaGuardada.getIdResena());

        return resenaMapper.toResponse(resenaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ResenaResponse obtenerPorId(String idResena) {
        log.info("Obteniendo reseña con ID: {}", idResena);

        Resena resena = resenaRepository.findById(idResena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + idResena));

        return resenaMapper.toResponse(resena);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResenaResponse> listarPorServicio(String idServicio, Pageable pageable) {
        log.info("Listando reseñas del servicio: {}", idServicio);

        // Validar que el servicio existe
        if (!servicioRepository.existsById(idServicio)) {
            throw new ResourceNotFoundException("Servicio no encontrado con ID: " + idServicio);
        }

        Page<Resena> resenas = resenaRepository.findByServicioIdServicio(idServicio, pageable);

        log.info("Se encontraron {} reseñas para el servicio {}", resenas.getTotalElements(), idServicio);

        return resenas.map(resenaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResenaResponse> listarPorCliente(String idCliente) {
        log.info("Listando reseñas del cliente: {}", idCliente);

        // Validar que el cliente existe
        if (!clienteRepository.existsById(idCliente)) {
            throw new ResourceNotFoundException("Cliente no encontrado con ID: " + idCliente);
        }

        List<Resena> resenas = resenaRepository.findByClienteIdUsuario(idCliente);

        log.info("Se encontraron {} reseñas del cliente {}", resenas.size(), idCliente);

        return resenaMapper.toResponseList(resenas);
    }

    @Override
    @Transactional
    public ResenaResponse responderResena(String idResena, String idProveedor, ResponderResenaRequest request) {
        log.info("Proveedor {} respondiendo reseña: {}", idProveedor, idResena);

        // Validar que el proveedor existe
        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + idProveedor));

        // Validar que la reseña existe
        Resena resena = resenaRepository.findById(idResena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + idResena));

        // Validar que el servicio pertenece al proveedor
        if (!resena.getServicio().getProveedor().getIdUsuario().equals(idProveedor)) {
            throw new BusinessException("Solo el proveedor dueño del servicio puede responder esta reseña");
        }

        // Validar que no tenga respuesta previa
        if (resena.getRespuestaProveedor() != null) {
            throw new BusinessException("Esta reseña ya tiene una respuesta");
        }

        // Agregar respuesta
        resena.setRespuestaProveedor(request.getRespuesta());
        resena.setFechaRespuesta(LocalDateTime.now());

        Resena resenaActualizada = resenaRepository.save(resena);

        log.info("Respuesta agregada exitosamente a la reseña: {}", idResena);

        return resenaMapper.toResponse(resenaActualizada);
    }

    @Override
    @Transactional
    public MessageResponse reportarResena(String idResena, String idUsuario) {
        log.info("Usuario {} reportando reseña: {}", idUsuario, idResena);

        // Validar que la reseña existe
        Resena resena = resenaRepository.findById(idResena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + idResena));

        // Validar que no esté reportada previamente
        if (resena.getReportada()) {
            throw new BusinessException("Esta reseña ya ha sido reportada y está en revisión");
        }

        // Marcar como reportada y cambiar estado a revisión
        resena.setReportada(true);
        resena.setEstadoRevision(EstadoRevision.EN_REVISION);

        resenaRepository.save(resena);

        log.info("Reseña {} marcada como reportada exitosamente", idResena);

        return MessageResponse.success("Reseña reportada exitosamente. Será revisada por un administrador.");
    }

    @Override
    @Transactional
    public MessageResponse eliminarResena(String idResena, String idCliente) {
        log.info("Cliente {} eliminando reseña: {}", idCliente, idResena);

        // Validar que la reseña existe
        Resena resena = resenaRepository.findById(idResena)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + idResena));

        // Validar que la reseña pertenece al cliente
        if (!resena.getCliente().getIdUsuario().equals(idCliente)) {
            throw new BusinessException("Solo el cliente dueño puede eliminar esta reseña");
        }

        // Obtener ID del servicio antes de eliminar
        String idServicio = resena.getServicio().getIdServicio();

        // Eliminar la reseña
        resenaRepository.delete(resena);

        // Actualizar calificación promedio del servicio
        actualizarCalificacionServicio(idServicio);

        log.info("Reseña {} eliminada exitosamente", idResena);

        return MessageResponse.success("Reseña eliminada exitosamente");
    }

    @Override
    @Transactional
    public void actualizarCalificacionServicio(String idServicio) {
        log.info("Actualizando calificación promedio del servicio: {}", idServicio);

        // Obtener el servicio
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con ID: " + idServicio));

        // Calcular promedio de calificaciones publicadas
        BigDecimal promedioCalificacion = resenaRepository.calcularPromedioCalificacion(idServicio);
        Long totalResenas = resenaRepository.contarResenasPorServicio(idServicio);

        // Actualizar servicio
        if (promedioCalificacion != null) {
            servicio.setCalificacionPromedio(promedioCalificacion.setScale(2, RoundingMode.HALF_UP));
        } else {
            servicio.setCalificacionPromedio(BigDecimal.ZERO);
        }

        servicio.setTotalResenas(totalResenas != null ? totalResenas.intValue() : 0);

        servicioRepository.save(servicio);

        // Actualizar calificación promedio del proveedor
        actualizarCalificacionProveedor(servicio.getProveedor().getIdUsuario());

        log.info("Calificación actualizada: {} estrellas ({} reseñas)",
                servicio.getCalificacionPromedio(), servicio.getTotalResenas());
    }

    /**
     * Actualiza la calificación promedio de un proveedor basado en todos sus servicios
     */
    private void actualizarCalificacionProveedor(String idProveedor) {
        log.debug("Actualizando calificación promedio del proveedor: {}", idProveedor);

        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + idProveedor));

        // Obtener todas las reseñas de los servicios del proveedor
        List<Resena> todasResenas = proveedor.getServiciosPublicados().stream()
                .flatMap(servicio -> resenaRepository.findResenasByServicio(servicio.getIdServicio()).stream())
                .toList();

        if (todasResenas.isEmpty()) {
            proveedor.setCalificacionPromedio(BigDecimal.ZERO);
        } else {
            double promedio = todasResenas.stream()
                    .mapToInt(Resena::getCalificacion)
                    .average()
                    .orElse(0.0);

            proveedor.setCalificacionPromedio(
                    BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP)
            );
        }

        proveedorRepository.save(proveedor);

        log.debug("Calificación del proveedor actualizada: {} estrellas",
                proveedor.getCalificacionPromedio());
    }
}