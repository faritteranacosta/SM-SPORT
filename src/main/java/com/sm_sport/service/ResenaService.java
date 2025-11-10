package com.sm_sport.service;

import com.sm_sport.dto.request.CrearResenaRequest;
import com.sm_sport.dto.request.ResponderResenaRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.ResenaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResenaService {

    /**
     * Crea una nueva reseña
     *
     * @param idCliente ID del cliente
     * @param request   Datos de la reseña
     * @return Reseña creada
     * @throws BusinessException si la reserva no está finalizada
     */
    ResenaResponse crearResena(String idCliente, CrearResenaRequest request);

    /**
     * Obtiene una reseña por su ID
     *
     * @param idResena ID de la reseña
     * @return Datos de la reseña
     */
    ResenaResponse obtenerPorId(String idResena);

    /**
     * Lista reseñas de un servicio
     *
     * @param idServicio ID del servicio
     * @param pageable   Configuración de paginación
     * @return Lista paginada de reseñas
     */
    Page<ResenaResponse> listarPorServicio(String idServicio, Pageable pageable);

    /**
     * Lista reseñas de un cliente
     *
     * @param idCliente ID del cliente
     * @return Lista de reseñas
     */
    List<ResenaResponse> listarPorCliente(String idCliente);

    /**
     * Responde a una reseña (proveedor)
     *
     * @param idResena    ID de la reseña
     * @param idProveedor ID del proveedor
     * @param request     Respuesta
     * @return Reseña actualizada
     */
    ResenaResponse responderResena(String idResena, String idProveedor, ResponderResenaRequest request);

    /**
     * Reporta una reseña como inapropiada
     *
     * @param idResena  ID de la reseña
     * @param idUsuario ID del usuario que reporta
     * @return Mensaje de confirmación
     */
    MessageResponse reportarResena(String idResena, String idUsuario);

    /**
     * Elimina una reseña
     *
     * @param idResena  ID de la reseña
     * @param idCliente ID del cliente
     * @return Mensaje de confirmación
     */
    MessageResponse eliminarResena(String idResena, String idCliente);

    /**
     * Calcula y actualiza calificación promedio de un servicio
     *
     * @param idServicio ID del servicio
     */
    void actualizarCalificacionServicio(String idServicio);
}
