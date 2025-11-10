package com.sm_sport.service;

import com.sm_sport.dto.request.CancelarReservaRequest;
import com.sm_sport.dto.request.CrearReservaRequest;
import com.sm_sport.dto.request.FiltroReservaRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PageResponse;
import com.sm_sport.dto.response.ReservaDetalleResponse;
import com.sm_sport.dto.response.ReservaResponse;

public interface ReservaService {

    /**
     * Crea una nueva reserva
     *
     * @param idCliente ID del cliente
     * @param request   Datos de la reserva
     * @return Reserva creada
     * @throws BusinessException si no hay disponibilidad
     */
    ReservaResponse crearReserva(String idCliente, CrearReservaRequest request);

    /**
     * Obtiene una reserva por su ID
     *
     * @param idReserva ID de la reserva
     * @return Datos de la reserva
     * @throws ResourceNotFoundException si no existe
     */
    ReservaResponse obtenerPorId(String idReserva);

    /**
     * Obtiene detalle completo de una reserva
     *
     * @param idReserva ID de la reserva
     * @return Detalle completo incluyendo servicio, pago y reseña
     */
    ReservaDetalleResponse obtenerDetalle(String idReserva);

    /**
     * Lista reservas del cliente autenticado
     *
     * @param idCliente ID del cliente
     * @param pagina    Número de página
     * @param tamano    Tamaño de página
     * @return Lista paginada de reservas
     */
    PageResponse<ReservaResponse> listarPorCliente(String idCliente, Integer pagina, Integer tamano);

    /**
     * Lista reservas recibidas por un proveedor
     *
     * @param idProveedor ID del proveedor
     * @param pagina      Número de página
     * @param tamano      Tamaño de página
     * @return Lista paginada de reservas
     */
    PageResponse<ReservaResponse> listarPorProveedor(String idProveedor, Integer pagina, Integer tamano);

    /**
     * Filtra reservas con múltiples criterios
     *
     * @param filtros Criterios de búsqueda
     * @return Lista paginada de reservas
     */
    PageResponse<ReservaResponse> filtrarReservas(FiltroReservaRequest filtros);

    /**
     * Confirma una reserva (proveedor)
     *
     * @param idReserva   ID de la reserva
     * @param idProveedor ID del proveedor
     * @return Reserva actualizada
     * @throws ForbiddenException si el usuario no es el proveedor
     */
    ReservaResponse confirmarReserva(String idReserva, String idProveedor);

    /**
     * Rechaza una reserva (proveedor)
     *
     * @param idReserva   ID de la reserva
     * @param idProveedor ID del proveedor
     * @param motivo      Motivo del rechazo
     * @return Reserva actualizada
     */
    ReservaResponse rechazarReserva(String idReserva, String idProveedor, String motivo);

    /**
     * Cancela una reserva (cliente)
     *
     * @param idReserva ID de la reserva
     * @param idCliente ID del cliente
     * @param request   Datos de cancelación
     * @return Mensaje de confirmación
     * @throws BusinessException si no se puede cancelar
     */
    MessageResponse cancelarReserva(String idReserva, String idCliente, CancelarReservaRequest request);

    /**
     * Finaliza una reserva
     *
     * @param idReserva ID de la reserva
     * @return Reserva actualizada
     */
    ReservaResponse finalizarReserva(String idReserva);

    /**
     * Verifica disponibilidad para una reserva
     *
     * @param idServicio ID del servicio
     * @param request    Datos de la reserva
     * @return true si está disponible
     */
    boolean verificarDisponibilidad(String idServicio, CrearReservaRequest request);

    /**
     * Cancela automáticamente reservas pendientes expiradas
     *
     * @return Cantidad de reservas canceladas
     */
    Integer cancelarReservasExpiradas();
}
