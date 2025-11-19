package com.sm_sport.controller;

import com.sm_sport.dto.request.ActualizarEstadoUsuarioRequest;
import com.sm_sport.dto.request.ActualizarPerfilRequest;
import com.sm_sport.dto.request.FiltroUsuarioRequest;
import com.sm_sport.dto.response.*;
import com.sm_sport.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de usuarios
 * Permite obtener, actualizar y gestionar perfiles de usuarios
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión de perfiles y usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene un usuario por su ID
     * Solo el propio usuario o administradores pueden acceder
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Obtiene la información de un usuario específico. Solo el propio usuario o administradores pueden acceder"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - No puedes ver este usuario",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<UsuarioResponse> obtenerUsuario(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable String id) {

        String idUsuarioAutenticado = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/usuarios/{} - Usuario autenticado: {}", id, idUsuarioAutenticado);

        // Validar que sea el propio usuario o un administrador
        if (!esAdministrador() && !id.equals(idUsuarioAutenticado)) {
            log.warn("Usuario {} intentó acceder a perfil de usuario {} sin permisos",
                    idUsuarioAutenticado, id);
            throw new com.sm_sport.exception.ForbiddenException(
                    "No tienes permisos para ver este usuario");
        }

        UsuarioResponse usuario = usuarioService.obtenerPorId(id);

        log.info("Usuario {} obtenido exitosamente", id);

        return ResponseEntity.ok(usuario);
    }

    /**
     * Obtiene el perfil completo del usuario autenticado
     */
    @GetMapping("/perfil")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener mi perfil",
            description = "Obtiene el perfil completo del usuario autenticado con información específica según su rol (Cliente, Proveedor o Administrador)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponse.class)
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
    public ResponseEntity<UsuarioResponse> obtenerPerfil() {
        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/usuarios/perfil - Usuario: {}", idUsuario);

        UsuarioResponse perfil = usuarioService.obtenerPerfil(idUsuario);

        log.info("Perfil obtenido exitosamente para usuario {}", idUsuario);

        return ResponseEntity.ok(perfil);
    }

    /**
     * Actualiza el perfil del usuario autenticado
     */
    @PutMapping("/perfil")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Actualizar mi perfil",
            description = "Permite al usuario actualizar su información personal: nombre, teléfono, dirección y campos específicos según su rol. El correo electrónico no se puede modificar"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos - Error de validación",
                    content = @Content
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
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            @Parameter(description = "Datos a actualizar en el perfil", required = true)
            @Valid @RequestBody ActualizarPerfilRequest request) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("PUT /api/v1/usuarios/perfil - Usuario: {}", idUsuario);

        UsuarioResponse usuarioActualizado = usuarioService.actualizarPerfil(idUsuario, request);

        log.info("Perfil actualizado exitosamente para usuario {}", idUsuario);

        return ResponseEntity.ok(usuarioActualizado);
    }

    /**
     * Elimina un usuario del sistema (soft delete)
     * Solo administradores pueden realizar esta acción
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Eliminar usuario",
            description = "[ADMIN] Desactiva un usuario del sistema (soft delete). El usuario pasa a estado INACTIVO pero sus datos se mantienen para auditoría"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario eliminado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El usuario ya está inactivo",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo administradores",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar", required = true)
            @PathVariable String id) {

        String idAdmin = obtenerIdUsuarioAutenticado();
        log.info("DELETE /api/v1/usuarios/{} - Admin: {}", id, idAdmin);

        MessageResponse response = usuarioService.eliminarUsuario(id);

        log.info("Usuario {} eliminado por administrador {}", id, idAdmin);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene información específica de un cliente
     */
    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener información de cliente",
            description = "Obtiene información detallada de un cliente específico con sus estadísticas de reservas y reseñas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<ClienteResponse> obtenerCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable String idCliente) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/usuarios/cliente/{} - Usuario: {}", idCliente, idUsuario);

        ClienteResponse cliente = usuarioService.obtenerCliente(idCliente);

        log.info("Información de cliente {} obtenida exitosamente", idCliente);

        return ResponseEntity.ok(cliente);
    }

    /**
     * Obtiene información específica de un proveedor
     */
    @GetMapping("/proveedor/{idProveedor}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener información de proveedor",
            description = "Obtiene información detallada de un proveedor con sus estadísticas, calificaciones y servicios publicados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Proveedor encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProveedorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<ProveedorResponse> obtenerProveedor(
            @Parameter(description = "ID del proveedor", required = true)
            @PathVariable String idProveedor) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/usuarios/proveedor/{} - Usuario: {}", idProveedor, idUsuario);

        ProveedorResponse proveedor = usuarioService.obtenerProveedor(idProveedor);

        log.info("Información de proveedor {} obtenida exitosamente", idProveedor);

        return ResponseEntity.ok(proveedor);
    }

    /**
     * Lista usuarios con filtros avanzados (solo administradores)
     * Este endpoint está duplicado en AdminController para mejor organización
     */
    @PostMapping("/buscar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Buscar usuarios con filtros",
            description = "[ADMIN] Busca usuarios aplicando múltiples filtros: nombre, correo, rol, estado, rango de fechas. Incluye paginación y ordenamiento"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda realizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo administradores",
                    content = @Content
            )
    })
    public ResponseEntity<PageResponse<UsuarioResponse>> buscarUsuarios(
            @Parameter(description = "Filtros de búsqueda", required = true)
            @Valid @RequestBody FiltroUsuarioRequest filtros) {

        String idAdmin = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/usuarios/buscar - Admin: {}", idAdmin);

        PageResponse<UsuarioResponse> usuarios = usuarioService.listarUsuarios(filtros);

        log.info("Búsqueda completada: {} usuarios encontrados", usuarios.getTotalElements());

        return ResponseEntity.ok(usuarios);
    }

    /**
     * Cambia el estado de un usuario (solo administradores)
     */
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Cambiar estado de usuario",
            description = "[ADMIN] Permite cambiar el estado de un usuario (ACTIVO, INACTIVO, SUSPENDIDO) con un motivo opcional"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estado inválido o el usuario ya tiene ese estado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo administradores",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> cambiarEstado(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable String id,

            @Parameter(description = "Nuevo estado y motivo del cambio", required = true)
            @Valid @RequestBody ActualizarEstadoUsuarioRequest request) {

        String idAdmin = obtenerIdUsuarioAutenticado();
        log.info("PUT /api/v1/usuarios/{}/estado - Admin: {} - Nuevo estado: {}",
                id, idAdmin, request.getEstado());

        MessageResponse response = usuarioService.cambiarEstado(id, request);

        log.info("Estado de usuario {} actualizado por administrador {}", id, idAdmin);

        return ResponseEntity.ok(response);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene el ID del usuario autenticado desde el contexto de seguridad
     */
    private String obtenerIdUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * Verifica si el usuario autenticado es administrador
     */
    private boolean esAdministrador() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }
}