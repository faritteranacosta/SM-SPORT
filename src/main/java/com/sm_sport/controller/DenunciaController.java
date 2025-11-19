package com.sm_sport.controller;

import com.sm_sport.dto.request.CrearDenunciaRequest;
import com.sm_sport.dto.request.ResponderDenunciaRequest;
import com.sm_sport.dto.response.DenunciaResponse;
import com.sm_sport.service.DenunciaService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de denuncias entre usuarios
 * Permite crear denuncias, consultarlas y gestionarlas (solo administradores)
 */
@RestController
@RequestMapping("/api/v1/denuncias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Denuncias", description = "Gestión de denuncias entre usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class DenunciaController {

    private final DenunciaService denunciaService;

    /**
     * Crea una nueva denuncia contra otro usuario
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR')")
    @Operation(
            summary = "Crear denuncia",
            description = "Permite a un usuario crear una denuncia contra otro usuario por comportamiento inapropiado, incumplimiento, fraude, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Denuncia creada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DenunciaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos - Error de validación o negocio",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario denunciado no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<DenunciaResponse> crearDenuncia(
            @Parameter(description = "Datos de la denuncia", required = true)
            @Valid @RequestBody CrearDenunciaRequest request) {

        String idDenunciante = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/denuncias - Usuario {} denunciando a {}",
                idDenunciante, request.getIdUsuarioDenunciado());

        DenunciaResponse denuncia = denunciaService.crearDenuncia(idDenunciante, request);

        log.info("Denuncia creada exitosamente con ID: {}", denuncia.getIdDenuncia());

        return ResponseEntity.status(HttpStatus.CREATED).body(denuncia);
    }

    /**
     * Obtiene una denuncia por su ID
     * Solo el denunciante, denunciado o admin pueden verla
     */
    @GetMapping("/{idDenuncia}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener denuncia por ID",
            description = "Obtiene los detalles de una denuncia específica. Solo el denunciante, denunciado o administradores pueden verla"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Denuncia encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DenunciaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - La denuncia no te pertenece",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Denuncia no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<DenunciaResponse> obtenerDenuncia(
            @Parameter(description = "ID de la denuncia", required = true)
            @PathVariable String idDenuncia) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/denuncias/{} - Usuario: {}", idDenuncia, idUsuario);

        DenunciaResponse denuncia = denunciaService.obtenerPorId(idDenuncia);

        // Validar que el usuario tenga permiso para ver esta denuncia
        // (es el denunciante, el denunciado, o es administrador)
        if (!esAdministrador() &&
                !denuncia.getIdUsuarioDenunciante().equals(idUsuario) &&
                !denuncia.getIdUsuarioDenunciado().equals(idUsuario)) {

            log.warn("Usuario {} intentó acceder a denuncia {} sin permisos",
                    idUsuario, idDenuncia);
            throw new com.sm_sport.exception.ForbiddenException(
                    "No tienes permisos para ver esta denuncia");
        }

        log.info("Denuncia {} obtenida por usuario {}", idDenuncia, idUsuario);

        return ResponseEntity.ok(denuncia);
    }

    /**
     * Lista las denuncias realizadas por el usuario autenticado
     */
    @GetMapping("/mis-denuncias")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR')")
    @Operation(
            summary = "Listar mis denuncias",
            description = "Obtiene todas las denuncias que el usuario autenticado ha realizado contra otros usuarios"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de denuncias obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DenunciaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<List<DenunciaResponse>> listarMisDenuncias() {
        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/denuncias/mis-denuncias - Usuario: {}", idUsuario);

        List<DenunciaResponse> denuncias = denunciaService.listarPorDenunciante(idUsuario);

        log.info("Usuario {} tiene {} denuncias realizadas", idUsuario, denuncias.size());

        return ResponseEntity.ok(denuncias);
    }

    /**
     * Lista las denuncias realizadas contra el usuario autenticado
     */
    @GetMapping("/contra-mi")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR')")
    @Operation(
            summary = "Listar denuncias contra mí",
            description = "Obtiene todas las denuncias que otros usuarios han realizado contra el usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de denuncias obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DenunciaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<List<DenunciaResponse>> listarDenunciasContraMi() {
        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/denuncias/contra-mi - Usuario: {}", idUsuario);

        List<DenunciaResponse> denuncias = denunciaService.listarContraUsuario(idUsuario);

        log.info("Usuario {} tiene {} denuncias en su contra", idUsuario, denuncias.size());

        return ResponseEntity.ok(denuncias);
    }

    /**
     * Lista todas las denuncias pendientes de revisión (solo administradores)
     */
    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Listar denuncias pendientes",
            description = "[ADMIN] Obtiene todas las denuncias que están pendientes de revisión y requieren atención del administrador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de denuncias pendientes obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DenunciaResponse.class)
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
    public ResponseEntity<List<DenunciaResponse>> listarDenunciasPendientes() {
        String idAdmin = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/denuncias/pendientes - Admin: {}", idAdmin);

        List<DenunciaResponse> denuncias = denunciaService.listarPendientes();

        log.info("Se encontraron {} denuncias pendientes", denuncias.size());

        return ResponseEntity.ok(denuncias);
    }

    /**
     * Responde a una denuncia y toma una acción (solo administradores)
     */
    @PutMapping("/{idDenuncia}/responder")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Responder denuncia",
            description = "[ADMIN] Permite al administrador responder a una denuncia y tomar una acción (advertencia, sanción, bloqueo, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Denuncia atendida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DenunciaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o denuncia ya procesada",
                    content = @Content
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Denuncia no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<DenunciaResponse> responderDenuncia(
            @Parameter(description = "ID de la denuncia", required = true)
            @PathVariable String idDenuncia,

            @Parameter(description = "Respuesta del administrador y acción tomada", required = true)
            @Valid @RequestBody ResponderDenunciaRequest request) {

        String idAdmin = obtenerIdUsuarioAutenticado();
        log.info("PUT /api/v1/denuncias/{}/responder - Admin: {}", idDenuncia, idAdmin);

        DenunciaResponse denuncia = denunciaService.responderDenuncia(idDenuncia, idAdmin, request);

        log.info("Denuncia {} respondida por admin {} con acción: {}",
                idDenuncia, idAdmin, request.getAccionTomada());

        return ResponseEntity.ok(denuncia);
    }

    /**
     * Declara una denuncia como improcedente (solo administradores)
     */
    @PutMapping("/{idDenuncia}/improcedente")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Declarar denuncia improcedente",
            description = "[ADMIN] Permite al administrador declarar una denuncia como improcedente sin tomar acciones contra el denunciado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Denuncia declarada improcedente exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DenunciaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Denuncia ya procesada",
                    content = @Content
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Denuncia no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<DenunciaResponse> declararImprocedente(
            @Parameter(description = "ID de la denuncia", required = true)
            @PathVariable String idDenuncia) {

        String idAdmin = obtenerIdUsuarioAutenticado();
        log.info("PUT /api/v1/denuncias/{}/improcedente - Admin: {}", idDenuncia, idAdmin);

        DenunciaResponse denuncia = denunciaService.declararImprocedente(idDenuncia, idAdmin);

        log.info("Denuncia {} declarada improcedente por admin {}", idDenuncia, idAdmin);

        return ResponseEntity.ok(denuncia);
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