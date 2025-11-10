package com.sm_sport.service;

import com.sm_sport.dto.request.CrearDenunciaRequest;
import com.sm_sport.dto.request.ResponderDenunciaRequest;
import com.sm_sport.dto.response.DenunciaResponse;

import java.util.List;

public interface DenunciaService {

    /**
     * Crea una nueva denuncia
     *
     * @param idDenunciante ID del usuario que denuncia
     * @param request       Datos de la denuncia
     * @return Denuncia creada
     */
    DenunciaResponse crearDenuncia(String idDenunciante, CrearDenunciaRequest request);

    /**
     * Obtiene una denuncia por su ID
     *
     * @param idDenuncia ID de la denuncia
     * @return Datos de la denuncia
     */
    DenunciaResponse obtenerPorId(String idDenuncia);

    /**
     * Lista denuncias realizadas por un usuario
     *
     * @param idUsuario ID del usuario
     * @return Lista de denuncias
     */
    List<DenunciaResponse> listarPorDenunciante(String idUsuario);

    /**
     * Lista denuncias contra un usuario
     *
     * @param idUsuario ID del usuario
     * @return Lista de denuncias
     */
    List<DenunciaResponse> listarContraUsuario(String idUsuario);

    /**
     * Lista denuncias pendientes (administrador)
     *
     * @return Lista de denuncias pendientes
     */
    List<DenunciaResponse> listarPendientes();

    /**
     * Responde a una denuncia (administrador)
     *
     * @param idDenuncia ID de la denuncia
     * @param idAdmin    ID del administrador
     * @param request    Respuesta y acción
     * @return Denuncia actualizada
     */
    DenunciaResponse responderDenuncia(String idDenuncia, String idAdmin, ResponderDenunciaRequest request);

    /**
     * Declara una denuncia como improcedente
     *
     * @param idDenuncia ID de la denuncia
     * @param idAdmin    ID del administrador
     * @return Denuncia actualizada
     */
    DenunciaResponse declararImprocedente(String idDenuncia, String idAdmin);
}
