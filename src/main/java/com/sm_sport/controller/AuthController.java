package com.sm_sport.controller;

import com.sm_sport.dto.request.CambiarContrasenaRequest;
import com.sm_sport.dto.request.LoginRequest;
import com.sm_sport.dto.request.RecuperarContrasenaRequest;
import com.sm_sport.dto.request.RegistroUsuarioRequest;
import com.sm_sport.dto.response.AuthResponse;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Autenticación", description = "Endpoints para registro, inicio de sesión y gestión de credenciales")
public class AuthController {

    private final AuthService authService;

    /**
     * Registrar un nuevo usuario (Cliente o Proveedor)
     * POST /api/v1/auth/registro
     */
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Permite registrar un nuevo usuario en el sistema como Cliente o Proveedor. " +
                    "La contraseña debe cumplir con los requisitos de seguridad (mayúscula, minúscula, número y carácter especial). " +
                    "Retorna un token JWT para autenticación inmediata."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de registro inválidos o correo ya registrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"El correo electrónico ya está registrado\", \"success\": false, \"timestamp\": \"2024-11-19T10:30:00\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(
            @Valid @RequestBody RegistroUsuarioRequest request
    ) {
        log.info("POST /api/v1/auth/registro - Registrando usuario: {} con rol: {}",
                request.getCorreo(), request.getRol());

        AuthResponse response = authService.registrar(request);

        log.info("Usuario registrado exitosamente: {} - ID: {} - Rol: {}",
                response.getCorreo(), response.getIdUsuario(), response.getRol());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Iniciar sesión
     * POST /api/v1/auth/login
     */
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con correo y contraseña. " +
                    "Si las credenciales son válidas y el usuario está activo, retorna un token JWT válido por 24 horas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas o usuario inactivo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Credenciales inválidas\", \"success\": false, \"timestamp\": \"2024-11-19T10:30:00\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("POST /api/v1/auth/login - Intento de login: {}", request.getCorreo());

        AuthResponse response = authService.login(request);

        log.info("Login exitoso para: {} - Rol: {}", response.getCorreo(), response.getRol());

        return ResponseEntity.ok(response);
    }

    /**
     * Cerrar sesión
     * POST /api/v1/auth/logout
     */
    @Operation(
            summary = "Cerrar sesión",
            description = "Cierra la sesión del usuario autenticado. " +
                    "El token JWT debe ser eliminado del lado del cliente. " +
                    "En una implementación con blacklist, el token se invalidaría en el servidor."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión cerrada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            )
    })
    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> logout() {
        String correo = obtenerCorreoUsuarioAutenticado();

        log.info("POST /api/v1/auth/logout - Usuario cerrando sesión: {}", correo);

        // TODO: Si implementas blacklist de tokens, invalidar el token aquí
        // tokenBlacklistService.invalidarToken(token);

        log.info("Sesión cerrada exitosamente para: {}", correo);

        return ResponseEntity.ok(
                MessageResponse.success("Sesión cerrada exitosamente")
        );
    }

    /**
     * Cambiar contraseña
     * PUT /api/v1/auth/cambiar-password
     */
    @Operation(
            summary = "Cambiar contraseña",
            description = "Permite al usuario autenticado cambiar su contraseña. " +
                    "Requiere la contraseña actual para validación y la nueva contraseña debe cumplir " +
                    "con los requisitos de seguridad (mayúscula, minúscula, número y carácter especial)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contraseña actualizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Contraseña actual incorrecta o las contraseñas no coinciden",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"La contraseña actual es incorrecta\", \"success\": false, \"timestamp\": \"2024-11-19T10:30:00\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    @PutMapping("/cambiar-password")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> cambiarContrasena(
            @Valid @RequestBody CambiarContrasenaRequest request
    ) {
        String idUsuario = obtenerIdUsuarioAutenticado();

        log.info("PUT /api/v1/auth/cambiar-password - Usuario {} cambiando contraseña", idUsuario);

        MessageResponse response = authService.cambiarContrasena(idUsuario, request);

        log.info("Contraseña cambiada exitosamente para usuario: {}", idUsuario);

        return ResponseEntity.ok(response);
    }

    /**
     * Recuperar contraseña
     * POST /api/v1/auth/recuperar
     */
    @Operation(
            summary = "Recuperar contraseña",
            description = "Inicia el proceso de recuperación de contraseña. " +
                    "Envía un correo electrónico al usuario con instrucciones y un token temporal " +
                    "para restablecer su contraseña. El endpoint es público."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Correo de recuperación enviado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Se ha enviado un correo con instrucciones para recuperar tu contraseña\", \"success\": true, \"timestamp\": \"2024-11-19T10:30:00\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado con el correo proporcionado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"message\": \"Usuario no encontrado\", \"success\": false, \"timestamp\": \"2024-11-19T10:30:00\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Correo inválido",
                    content = @Content
            )
    })
    @PostMapping("/recuperar")
    public ResponseEntity<MessageResponse> recuperarContrasena(
            @Valid @RequestBody RecuperarContrasenaRequest request
    ) {
        log.info("POST /api/v1/auth/recuperar - Solicitud de recuperación para: {}", request.getCorreo());

        MessageResponse response = authService.recuperarContrasena(request);

        log.info("Correo de recuperación enviado a: {}", request.getCorreo());

        return ResponseEntity.ok(response);
    }

    /**
     * Validar token JWT
     * GET /api/v1/auth/validar-token
     */
    @Operation(
            summary = "Validar token JWT",
            description = "Verifica si un token JWT es válido y no ha expirado. " +
                    "Útil para validaciones del lado del cliente antes de realizar operaciones sensibles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token validado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"valid\": true}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"valid\": false}"
                            )
                    )
            )
    })
    @GetMapping("/validar-token")
    public ResponseEntity<?> validarToken(
            @Parameter(description = "Token JWT a validar", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestParam String token
    ) {
        log.info("GET /api/v1/auth/validar-token - Validando token");

        boolean valido = authService.validarToken(token);

        log.info("Token validado: {}", valido);

        return ResponseEntity.ok(new TokenValidationResponse(valido));
    }

    /**
     * Refrescar token (funcionalidad futura)
     * POST /api/v1/auth/refresh-token
     */
    @Operation(
            summary = "Refrescar token JWT",
            description = "Genera un nuevo token JWT usando un refresh token válido. " +
                    "Funcionalidad pendiente de implementación."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refrescado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido o expirado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "501",
                    description = "Funcionalidad no implementada",
                    content = @Content
            )
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refrescarToken(
            @Parameter(description = "Refresh token", required = true)
            @RequestParam String refreshToken
    ) {
        log.info("POST /api/v1/auth/refresh-token - Refrescando token");

        // TODO: Implementar cuando esté listo en el servicio
        AuthResponse response = authService.refrescarToken(refreshToken);

        log.info("Token refrescado exitosamente");

        return ResponseEntity.ok(response);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene el ID del usuario autenticado desde el contexto de seguridad
     */
    private String obtenerIdUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Asume que el username es el ID del usuario
    }

    /**
     * Obtiene el correo del usuario autenticado desde el contexto de seguridad
     */
    private String obtenerCorreoUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Asume que el username es el correo
    }

    // ==================== CLASE INTERNA PARA RESPUESTA ====================

    /**
     * Clase auxiliar para respuesta de validación de token
     */
    private record TokenValidationResponse(boolean valid) {}
}