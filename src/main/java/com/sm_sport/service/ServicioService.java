package com.sm_sport.service;

import com.sm_sport.dto.request.ActualizarServicioRequest;
import com.sm_sport.dto.request.BusquedaServicioRequest;
import com.sm_sport.dto.request.CrearServicioRequest;
import com.sm_sport.dto.request.DisponibilidadRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PageResponse;
import com.sm_sport.dto.response.ServicioDetalleResponse;
import com.sm_sport.dto.response.ServicioResponse;
import com.sm_sport.model.enums.EstadoServicio;

import java.util.List;

public interface ServicioService {

    /**
     * Publica un nuevo servicio
     *
     * @param idProveedor ID del proveedor
     * @param request     Datos del servicio
     * @return Servicio creado
     * @throws BusinessException si el proveedor no tiene saldo suficiente
     */
    ServicioResponse publicarServicio(String idProveedor, CrearServicioRequest request);

    /**
     * Obtiene un servicio por su ID
     *
     * @param idServicio ID del servicio
     * @return Datos básicos del servicio
     * @throws ResourceNotFoundException si no existe
     */
    ServicioResponse obtenerPorId(String idServicio);

    /**
     * Obtiene detalle completo de un servicio
     *
     * @param idServicio ID del servicio
     * @return Datos detallados incluyendo reseñas y disponibilidad
     */
    ServicioDetalleResponse obtenerDetalle(String idServicio);

    /**
     * Actualiza un servicio existente
     *
     * @param idServicio ID del servicio
     * @param request    Datos a actualizar
     * @return Servicio actualizado
     * @throws ForbiddenException si el usuario no es el propietario
     */
    ServicioResponse actualizarServicio(String idServicio, ActualizarServicioRequest request);

    /**
     * Cambia el estado de un servicio
     *
     * @param idServicio  ID del servicio
     * @param nuevoEstado Nuevo estado
     * @return Servicio actualizado
     */
    ServicioResponse cambiarEstado(String idServicio, EstadoServicio nuevoEstado);

    /**
     * Elimina un servicio (soft delete)
     *
     * @param idServicio ID del servicio
     * @return Mensaje de confirmación
     * @throws BusinessException si tiene reservas activas
     */
    MessageResponse eliminarServicio(String idServicio);

    /**
     * Lista servicios con paginación
     *
     * @param pagina Número de página
     * @param tamano Tamaño de página
     * @return Lista paginada de servicios
     */
    PageResponse<ServicioResponse> listarServicios(Integer pagina, Integer tamano);

    /**
     * Lista servicios de un proveedor
     *
     * @param idProveedor ID del proveedor
     * @return Lista de servicios
     */
    List<ServicioResponse> listarPorProveedor(String idProveedor);

    /**
     * Busca servicios con múltiples filtros
     *
     * @param filtros Criterios de búsqueda
     * @return Lista paginada de servicios
     */
    PageResponse<ServicioResponse> buscarServicios(BusquedaServicioRequest filtros);

    /**
     * Busca servicios cercanos a una ubicación
     *
     * @param latitud  Latitud
     * @param longitud Longitud
     * @param radioKm  Radio en kilómetros
     * @return Lista de servicios cercanos
     */
    List<ServicioResponse> buscarServiciosCercanos(Double latitud, Double longitud, Integer radioKm);

    /**
     * Agrega disponibilidad a un servicio
     *
     * @param idServicio       ID del servicio
     * @param disponibilidades Lista de disponibilidades
     * @return Mensaje de confirmación
     */
    MessageResponse agregarDisponibilidad(String idServicio, List<DisponibilidadRequest> disponibilidades);
}
