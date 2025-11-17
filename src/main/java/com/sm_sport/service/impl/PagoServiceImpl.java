package com.sm_sport.service.impl;

import com.sm_sport.dto.request.PagoRequest;
import com.sm_sport.dto.response.ComprobanteResponse;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PagoResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.PaymentException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.PagoMapper;
import com.sm_sport.model.entity.Cliente;
import com.sm_sport.model.entity.Comprobante;
import com.sm_sport.model.entity.Pago;
import com.sm_sport.model.entity.Reserva;
import com.sm_sport.model.enums.EstadoPago;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.repository.ClienteRepository;
import com.sm_sport.repository.ComprobanteRepository;
import com.sm_sport.repository.PagoRepository;
import com.sm_sport.repository.ReservaRepository;
//import com.sm_sport.service.NotificacionService;
import com.sm_sport.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final PagoMapper pagoMapper;
   // private final NotificacionService notificacionService;

    @Override
    public PagoResponse procesarPago(String idCliente, PagoRequest request) {
        log.info("Procesando pago para cliente: {}", idCliente);

        // Verificar cliente
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // Verificar reserva
        Reserva reserva = reservaRepository.findById(request.getIdReserva())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Validar que la reserva sea del cliente
        if (!reserva.getCliente().getIdUsuario().equals(idCliente)) {
            throw new BusinessException("La reserva no pertenece al cliente");
        }

        // Validar que la reserva esté pendiente
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new BusinessException("La reserva no está en estado pendiente");
        }

        // Validar que no tenga un pago existente
        if (pagoRepository.findByReservaIdReserva(request.getIdReserva()).isPresent()) {
            throw new BusinessException("La reserva ya tiene un pago asociado");
        }

        // Validar datos de pago
        if (!validarDatosPago(request)) {
            throw new PaymentException("Datos de pago inválidos");
        }

        // Crear pago
        Pago pago = pagoMapper.toEntity(request);
        pago.setReserva(reserva);
        pago.setCliente(cliente);
        pago.setMonto(reserva.getCostoTotal());
        pago.setEstadoPago(EstadoPago.PENDIENTE);
        pago.setReferenciaPago(generarReferenciaPago());

        try {
            // Simular procesamiento con pasarela de pagos
            // TODO: Integrar con pasarela real (Stripe, PayU, etc.)
            boolean pagoExitoso = procesarConPasarela(pago, request);

            if (pagoExitoso) {
                pago.setEstadoPago(EstadoPago.APROBADO);
                pago.setFechaAprobacion(LocalDateTime.now());
                pago.setProveedorPasarela("STRIPE"); // Ejemplo

                // Actualizar estado de reserva
                reserva.setEstado(EstadoReserva.CONFIRMADA);
                reservaRepository.save(reserva);

                // Generar comprobante
                generarComprobanteAutomatico(pago);

                // Notificar al cliente
//                notificacionService.enviarNotificacion(
//                        idCliente,
//                        "PAGO",
//                        "Pago aprobado",
//                        "Tu pago ha sido procesado exitosamente"
//                );

                // Notificar al proveedor
//                notificacionService.enviarNotificacion(
//                        reserva.getProveedor().getIdUsuario(),
//                        "PAGO",
//                        "Nuevo pago recibido",
//                        "Has recibido un pago por una reserva"
//                );

                log.info("Pago procesado exitosamente: {}", pago.getIdPago());
            } else {
                pago.setEstadoPago(EstadoPago.RECHAZADO);

                log.warn("Pago rechazado para reserva: {}", request.getIdReserva());
                throw new PaymentException("El pago fue rechazado por la pasarela");
            }

        } catch (Exception e) {
            pago.setEstadoPago(EstadoPago.RECHAZADO);
            log.error("Error procesando pago: {}", e.getMessage());
            throw new PaymentException("Error al procesar el pago: " + e.getMessage());
        }

        pago = pagoRepository.save(pago);

        return pagoMapper.toResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(String idPago) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        return pagoMapper.toResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPorReserva(String idReserva) {
        Pago pago = pagoRepository.findByReservaIdReserva(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        return pagoMapper.toResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorCliente(String idCliente) {
        List<Pago> pagos = pagoRepository.findByClienteIdUsuario(idCliente);
        return pagoMapper.toResponseList(pagos);
    }

    @Override
    public ComprobanteResponse generarComprobante(String idPago) {
        log.info("Generando comprobante para pago: {}", idPago);

        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        // Verificar que el pago esté aprobado
        if (pago.getEstadoPago() != EstadoPago.APROBADO) {
            throw new BusinessException("Solo se pueden generar comprobantes de pagos aprobados");
        }

        // Verificar si ya existe comprobante
        Comprobante comprobanteExistente = comprobanteRepository.findByPagoIdPago(idPago).orElse(null);

        if (comprobanteExistente != null) {
            return pagoMapper.toComprobanteResponse(comprobanteExistente);
        }

        // Crear comprobante
        Comprobante comprobante = Comprobante.builder()
                .pago(pago)
                .monto(pago.getMonto())
                .detalle(String.format("Comprobante de pago - Reserva #%s", pago.getReserva().getIdReserva()))
                .formato("PDF")
                .urlArchivo("/comprobantes/" + pago.getIdPago() + ".pdf") // TODO: Generar PDF real
                .build();

        comprobante = comprobanteRepository.save(comprobante);

        log.info("Comprobante generado: {}", comprobante.getIdComprobante());

        return pagoMapper.toComprobanteResponse(comprobante);
    }

    @Override
    public MessageResponse procesarReembolso(String idPago) {
        log.info("Procesando reembolso para pago: {}", idPago);

        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        // Validar estado
        if (pago.getEstadoPago() != EstadoPago.APROBADO) {
            throw new BusinessException("Solo se pueden reembolsar pagos aprobados");
        }

        try {
            // TODO: Integrar con pasarela para reembolso real
            boolean reembolsoExitoso = procesarReembolsoConPasarela(pago);

            if (reembolsoExitoso) {
                pago.setEstadoPago(EstadoPago.REEMBOLSADO);
                pagoRepository.save(pago);

//                // Notificar al cliente
//                notificacionService.enviarNotificacion(
//                        pago.getCliente().getIdUsuario(),
//                        "PAGO",
//                        "Reembolso procesado",
//                        "Tu reembolso ha sido procesado exitosamente"
//                );

                log.info("Reembolso procesado exitosamente");

                return MessageResponse.success("Reembolso procesado exitosamente");
            } else {
                throw new PaymentException("Error al procesar el reembolso");
            }

        } catch (Exception e) {
            log.error("Error procesando reembolso: {}", e.getMessage());
            throw new PaymentException("Error al procesar reembolso: " + e.getMessage());
        }
    }

    @Override
    public boolean validarDatosPago(PagoRequest request) {
        // Validaciones básicas
        if (request.getMetodoPago() == null) {
            return false;
        }

        // Validar según método de pago
        switch (request.getMetodoPago()) {
            case TARJETA_CREDITO, TARJETA_DEBITO -> {
                // Validar datos de tarjeta
                return request.getNumeroTarjeta() != null
                        && request.getNombreTitular() != null
                        && request.getFechaExpiracion() != null
                        && request.getCvv() != null;
            }
            case BILLETERA_DIGITAL -> {
                return request.getEmailBilletera() != null;
            }
            case TRANSFERENCIA_BANCARIA -> {
                return true; // Validaciones adicionales según requerimientos
            }
            default -> {
                return false;
            }
        }
    }

    // Métodos privados auxiliares

    private String generarReferenciaPago() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean procesarConPasarela(Pago pago, PagoRequest request) {
        // Simulación de procesamiento con pasarela
        // TODO: Implementar integración real con Stripe, PayU, MercadoPago, etc.

        log.info("Procesando pago con pasarela: {} - Monto: {}",
                request.getMetodoPago(), pago.getMonto());

        // Simulación: 95% de pagos exitosos
        return Math.random() < 0.95;
    }

    private boolean procesarReembolsoConPasarela(Pago pago) {
        // Simulación de reembolso
        // TODO: Implementar integración real

        log.info("Procesando reembolso con pasarela - Pago: {}", pago.getIdPago());

        return true;
    }

    private void generarComprobanteAutomatico(Pago pago) {
        Comprobante comprobante = Comprobante.builder()
                .pago(pago)
                .monto(pago.getMonto())
                .detalle(String.format("Comprobante de pago - Reserva #%s", pago.getReserva().getIdReserva()))
                .formato("PDF")
                .urlArchivo("/comprobantes/" + pago.getIdPago() + ".pdf")
                .build();

        comprobanteRepository.save(comprobante);
    }
}
