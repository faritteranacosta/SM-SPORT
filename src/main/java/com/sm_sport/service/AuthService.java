package com.sm_sport.service;

import com.sm_sport.dto.request.CambiarContrasenaRequest;
import com.sm_sport.dto.request.LoginRequest;
import com.sm_sport.dto.request.RecuperarContrasenaRequest;
import com.sm_sport.dto.request.RegistroUsuarioRequest;
import com.sm_sport.dto.response.AuthResponse;
import com.sm_sport.dto.response.MessageResponse;

public interface AuthService {

    /**
     * Registra un nuevo usuario en el sistema
     *
     * @param request Datos del usuario a registrar
     * @return Token JWT y datos del usuario
     * @throws BusinessException si el correo ya está registrado
     */
    AuthResponse registrar(RegistroUsuarioRequest request);

    /**
     * Autentica un usuario y genera token JWT
     *
     * @param request Credenciales de acceso
     * @return Token JWT y datos del usuario
     * @throws UnauthorizedException si las credenciales son inválidas
     */
    AuthResponse login(LoginRequest request);

    /**
     * Cambia la contraseña del usuario autenticado
     *
     * @param idUsuario ID del usuario
     * @param request   Datos de cambio de contraseña
     * @return Mensaje de confirmación
     * @throws BusinessException si la contraseña actual es incorrecta
     */
    MessageResponse cambiarContrasena(String idUsuario, CambiarContrasenaRequest request);

    /**
     * Inicia proceso de recuperación de contraseña
     *
     * @param request Datos de recuperación
     * @return Mensaje de confirmación
     */
    MessageResponse recuperarContrasena(RecuperarContrasenaRequest request);

    /**
     * Valida un token JWT
     *
     * @param token Token a validar
     * @return true si es válido, false en caso contrario
     */
    boolean validarToken(String token);

    /**
     * Refresca un token JWT expirado
     *
     * @param refreshToken Token de refresco
     * @return Nuevo token JWT
     */
    AuthResponse refrescarToken(String refreshToken);
}
