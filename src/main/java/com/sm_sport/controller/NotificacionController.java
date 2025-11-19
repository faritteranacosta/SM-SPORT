package com.sm_sport.controller;

import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.NotificacionResponse;
import com.sm_sport.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de notificaciones
 * Permite a los usuarios ver y gestionar sus notificaciones
 */
@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notificaciones", description = "Gestión de notificaciones de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class NotificacionController {

    private final NotificacionService notificacionService;

    /**
     * Obtiene todas las notificaciones del usuario autenticado
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Listar notificaciones del usuario",
            description = "Obtiene todas las notificaciones del usuario autenticado ordenadas por fecha"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de notificaciones obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificacionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<List<NotificacionResponse>> listarNotificaciones() {
        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/notificaciones - Usuario: {}", idUsuario);

        List<NotificacionResponse> notificaciones = notificacionService.listarPorUsuario(idUsuario);

        log.info("Se obtuvieron {} notificaciones para el usuario {}",
                notificaciones.size(), idUsuario);

        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Obtiene solo las notificaciones no leídas del usuario
     */
    @GetMapping("/no-leidas")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Listar notificaciones no leídas",
            description = "Obtiene únicamente las notificaciones que no han sido leídas por el usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de notificaciones no leídas obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificacionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<List<NotificacionResponse>> listarNoLeidas() {
        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/notificaciones/no-leidas - Usuario: {}", idUsuario);

        List<NotificacionResponse> notificaciones = notificacionService.listarNoLeidas(idUsuario);

        log.info("Usuario {} tiene {} notificaciones no leídas",
                idUsuario, notificaciones.size());

        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Marca una notificación específica como leída
     */
    @PutMapping("/{idNotificacion}/leer")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Marcar notificación como leída",
            description = "Marca una notificación específica como leída por el usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notificación marcada como leída exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notificación no encontrada o no pertenece al usuario",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> marcarComoLeida(
            @Parameter(description = "ID de la notificación a marcar como leída", required = true)
            @PathVariable String idNotificacion) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("PUT /api/v1/notificaciones/{}/leer - Usuario: {}", idNotificacion, idUsuario);

        MessageResponse response = notificacionService.marcarComoLeida(idNotificacion, idUsuario);

        log.info("Notificación {} marcada como leída por usuario {}",
                idNotificacion, idUsuario);

        return ResponseEntity.ok(response);
    }

    /**
     * Marca todas las notificaciones del usuario como leídas
     */
    @PutMapping("/marcar-todas-leidas")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Marcar todas como leídas",
            description = "Marca todas las notificaciones pendientes del usuario como leídas en una sola operación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Todas las notificaciones marcadas como leídas exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> marcarTodasComoLeidas() {
        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("PUT /api/v1/notificaciones/marcar-todas-leidas - Usuario: {}", idUsuario);

        MessageResponse response = notificacionService.marcarTodasComoLeidas(idUsuario);

        log.info("Todas las notificaciones del usuario {} marcadas como leídas", idUsuario);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de prueba para enviar notificación manual (solo para desarrollo/testing)
     * En producción, este endpoint debe ser eliminado o protegido con rol ADMINISTRADOR
     */
    @PostMapping("/test/enviar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "[TEST] Enviar notificación de prueba",
            description = "Endpoint de desarrollo para enviar notificaciones de prueba. Solo disponible para administradores"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Notificación enviada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo administradores",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> enviarNotificacionTest(
            @Parameter(description = "Tipo de notificación", example = "SISTEMA")
            @RequestParam(defaultValue = "SISTEMA") String tipo,

            @Parameter(description = "Título de la notificación", example = "Notificación de prueba")
            @RequestParam String titulo,

            @Parameter(description = "Mensaje de la notificación", example = "Este es un mensaje de prueba")
            @RequestParam String mensaje) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/notificaciones/test/enviar - Admin: {}", idUsuario);

        notificacionService.enviarNotificacion(idUsuario, tipo, titulo, mensaje);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.success("Notificación de prueba enviada"));
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene el ID del usuario autenticado desde el contexto de seguridad
     */
    private String obtenerIdUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Retorna el ID del usuario (username en JWT)
    }
}