package com.sm_sport.service;

import com.sm_sport.dto.request.PagoRequest;
import com.sm_sport.dto.response.ComprobanteResponse;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PagoResponse;

import java.util.List;

public interface PagoService {

    /**
     * Procesa un pago
     *
     * @param idCliente ID del cliente
     * @param request   Datos del pago
     * @return Información del pago procesado
     * @throws PaymentException si el pago falla
     */
    PagoResponse procesarPago(String idCliente, PagoRequest request);

    /**
     * Obtiene información de un pago
     *
     * @param idPago ID del pago
     * @return Datos del pago
     * @throws ResourceNotFoundException si no existe
     */
    PagoResponse obtenerPorId(String idPago);

    /**
     * Obtiene el pago asociado a una reserva
     *
     * @param idReserva ID de la reserva
     * @return Datos del pago
     */
    PagoResponse obtenerPorReserva(String idReserva);

    /**
     * Lista pagos de un cliente
     *
     * @param idCliente ID del cliente
     * @return Lista de pagos
     */
    List<PagoResponse> listarPorCliente(String idCliente);

    /**
     * Genera comprobante de pago
     *
     * @param idPago ID del pago
     * @return Información del comprobante
     */
    ComprobanteResponse generarComprobante(String idPago);

    /**
     * Procesa un reembolso
     *
     * @param idPago ID del pago
     * @return Mensaje de confirmación
     */
    MessageResponse procesarReembolso(String idPago);

    /**
     * Valida datos de pago
     *
     * @param request Datos a validar
     * @return true si son válidos
     */
    boolean validarDatosPago(PagoRequest request);
}
