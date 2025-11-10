package com.sm_sport.service;

import com.sm_sport.dto.request.ActualizarEstadoUsuarioRequest;
import com.sm_sport.dto.request.ActualizarPerfilRequest;
import com.sm_sport.dto.request.FiltroUsuarioRequest;
import com.sm_sport.dto.response.*;

public interface UsuarioService {

    /**
     * Obtiene un usuario por su ID
     *
     * @param idUsuario ID del usuario
     * @return Datos del usuario
     * @throws ResourceNotFoundException si no existe
     */
    UsuarioResponse obtenerPorId(String idUsuario);

    /**
     * Obtiene el perfil del usuario autenticado
     *
     * @param idUsuario ID del usuario autenticado
     * @return Datos del perfil
     */
    UsuarioResponse obtenerPerfil(String idUsuario);

    /**
     * Actualiza el perfil del usuario
     *
     * @param idUsuario ID del usuario
     * @param request   Datos a actualizar
     * @return Usuario actualizado
     */
    UsuarioResponse actualizarPerfil(String idUsuario, ActualizarPerfilRequest request);

    /**
     * Lista usuarios con filtros (solo administrador)
     *
     * @param filtros Criterios de búsqueda
     * @return Lista paginada de usuarios
     */
    PageResponse<UsuarioResponse> listarUsuarios(FiltroUsuarioRequest filtros);

    /**
     * Cambia el estado de un usuario (solo administrador)
     *
     * @param idUsuario ID del usuario
     * @param request   Nuevo estado
     * @return Mensaje de confirmación
     */
    MessageResponse cambiarEstado(String idUsuario, ActualizarEstadoUsuarioRequest request);

    /**
     * Elimina un usuario (solo administrador)
     *
     * @param idUsuario ID del usuario
     * @return Mensaje de confirmación
     */
    MessageResponse eliminarUsuario(String idUsuario);

    /**
     * Obtiene información de un cliente
     *
     * @param idCliente ID del cliente
     * @return Datos del cliente
     */
    ClienteResponse obtenerCliente(String idCliente);

    /**
     * Obtiene información de un proveedor
     *
     * @param idProveedor ID del proveedor
     * @return Datos del proveedor
     */
    ProveedorResponse obtenerProveedor(String idProveedor);

    /**
     * Verifica si un correo ya está registrado
     *
     * @param correo Correo a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existeCorreo(String correo);
}
