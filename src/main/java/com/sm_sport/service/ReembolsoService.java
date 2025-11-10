package com.sm_sport.service;

import com.sm_sport.dto.request.ReembolsoRequest;
import com.sm_sport.dto.response.SolicitudReembolsoResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ReembolsoService {

    /**
     * Solicita un reembolso
     *
     * @param idCliente ID del cliente
     * @param request   Datos de la solicitud
     * @return Solicitud creada
     */
    SolicitudReembolsoResponse solicitarReembolso(String idCliente, ReembolsoRequest request);

    /**
     * Obtiene una solicitud por su ID
     *
     * @param idSolicitud ID de la solicitud
     * @return Datos de la solicitud
     */
    SolicitudReembolsoResponse obtenerPorId(String idSolicitud);

    /**
     * Lista solicitudes de un cliente
     *
     * @param idCliente ID del cliente
     * @return Lista de solicitudes
     */
    List<SolicitudReembolsoResponse> listarPorCliente(String idCliente);

    /**
     * Lista solicitudes pendientes (administrador)
     *
     * @return Lista de solicitudes pendientes
     */
    List<SolicitudReembolsoResponse> listarPendientes();

    /**
     * Aprueba una solicitud de reembolso
     *
     * @param idSolicitud ID de la solicitud
     * @param idAdmin     ID del administrador
     * @return Solicitud actualizada
     */
    SolicitudReembolsoResponse aprobarReembolso(String idSolicitud, String idAdmin);

    /**
     * Rechaza una solicitud de reembolso
     *
     * @param idSolicitud ID de la solicitud
     * @param idAdmin     ID del administrador
     * @param motivo      Motivo del rechazo
     * @return Solicitud actualizada
     */
    SolicitudReembolsoResponse rechazarReembolso(String idSolicitud, String idAdmin, String motivo);

    /**
     * Calcula el monto de reembolso según políticas
     *
     * @param idReserva ID de la reserva
     * @return Monto a reembolsar
     */
    BigDecimal calcularMontoReembolso(String idReserva);
}
