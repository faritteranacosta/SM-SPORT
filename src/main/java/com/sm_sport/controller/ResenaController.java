package com.sm_sport.controller;

import com.sm_sport.dto.request.CrearResenaRequest;
import com.sm_sport.dto.request.ResponderResenaRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.ResenaResponse;
import com.sm_sport.service.ResenaService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de reseñas y calificaciones
 * Permite a los clientes calificar servicios y a los proveedores responder
 */
@RestController
@RequestMapping("/api/v1/resenas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reseñas", description = "Gestión de reseñas y calificaciones de servicios deportivos")
@SecurityRequirement(name = "bearerAuth")
public class ResenaController {

    private final ResenaService resenaService;

    /**
     * Crea una nueva reseña para un servicio
     * Solo clientes con reserva finalizada pueden crear reseñas
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Crear reseña",
            description = "Permite a un cliente crear una reseña y calificación para un servicio que haya consumido. Solo se puede calificar reservas finalizadas y una sola vez por reserva"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reseña creada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResenaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos - La reserva no está finalizada o ya existe una reseña",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo clientes pueden crear reseñas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<ResenaResponse> crearResena(
            @Parameter(description = "Datos de la reseña: calificación (1-5), comentario y ID de reserva", required = true)
            @Valid @RequestBody CrearResenaRequest request) {

        String idCliente = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/resenas - Cliente: {} - Reserva: {}",
                idCliente, request.getIdReserva());

        ResenaResponse resena = resenaService.crearResena(idCliente, request);

        log.info("Reseña creada exitosamente con ID: {} - Calificación: {}",
                resena.getIdResena(), resena.getCalificacion());

        return ResponseEntity.status(HttpStatus.CREATED).body(resena);
    }

    /**
     * Obtiene una reseña específica por su ID
     */
    @GetMapping("/{idResena}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener reseña por ID",
            description = "Obtiene los detalles completos de una reseña específica, incluyendo la respuesta del proveedor si existe"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reseña encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResenaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reseña no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<ResenaResponse> obtenerResena(
            @Parameter(description = "ID de la reseña", required = true)
            @PathVariable String idResena) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/resenas/{} - Usuario: {}", idResena, idUsuario);

        ResenaResponse resena = resenaService.obtenerPorId(idResena);

        log.info("Reseña {} obtenida exitosamente", idResena);

        return ResponseEntity.ok(resena);
    }

    /**
     * Lista todas las reseñas de un servicio específico
     * Incluye paginación y ordenamiento
     */
    @GetMapping("/servicio/{idServicio}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Listar reseñas de un servicio",
            description = "Obtiene todas las reseñas publicadas de un servicio específico con paginación. Útil para que los usuarios vean opiniones antes de reservar"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de reseñas obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResenaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<Page<ResenaResponse>> listarResenasPorServicio(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String idServicio,

            @Parameter(description = "Número de página (inicia en 0)")
            @RequestParam(defaultValue = "0") int pagina,

            @Parameter(description = "Cantidad de elementos por página")
            @RequestParam(defaultValue = "10") int tamano,

            @Parameter(description = "Campo por el cual ordenar")
            @RequestParam(defaultValue = "fechaCreacion") String ordenarPor,

            @Parameter(description = "Dirección del ordenamiento (ASC o DESC)")
            @RequestParam(defaultValue = "DESC") String direccion) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/resenas/servicio/{} - Usuario: {} - Página: {}/{}",
                idServicio, idUsuario, pagina, tamano);

        // Configurar paginación y ordenamiento
        Sort sort = Sort.by(
                direccion.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                ordenarPor
        );
        Pageable pageable = PageRequest.of(pagina, tamano, sort);

        Page<ResenaResponse> resenas = resenaService.listarPorServicio(idServicio, pageable);

        log.info("Se encontraron {} reseñas para el servicio {}",
                resenas.getTotalElements(), idServicio);

        return ResponseEntity.ok(resenas);
    }

    /**
     * Lista todas las reseñas creadas por el cliente autenticado
     */
    @GetMapping("/mis-resenas")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Listar mis reseñas",
            description = "Obtiene todas las reseñas que el cliente autenticado ha creado, incluyendo las respuestas de los proveedores"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de reseñas obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResenaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo clientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<List<ResenaResponse>> listarMisResenas() {
        String idCliente = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/resenas/mis-resenas - Cliente: {}", idCliente);

        List<ResenaResponse> resenas = resenaService.listarPorCliente(idCliente);

        log.info("Cliente {} tiene {} reseñas creadas", idCliente, resenas.size());

        return ResponseEntity.ok(resenas);
    }

    /**
     * Permite al proveedor responder a una reseña de su servicio
     */
    @PostMapping("/{idResena}/responder")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Responder a una reseña",
            description = "Permite al proveedor dueño del servicio responder a una reseña de un cliente. Solo se puede responder una vez por reseña"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Respuesta agregada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResenaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La reseña ya tiene una respuesta o datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo el proveedor dueño del servicio puede responder",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reseña no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<ResenaResponse> responderResena(
            @Parameter(description = "ID de la reseña a responder", required = true)
            @PathVariable String idResena,

            @Parameter(description = "Respuesta del proveedor", required = true)
            @Valid @RequestBody ResponderResenaRequest request) {

        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/resenas/{}/responder - Proveedor: {}",
                idResena, idProveedor);

        ResenaResponse resena = resenaService.responderResena(idResena, idProveedor, request);

        log.info("Proveedor {} respondió exitosamente a reseña {}",
                idProveedor, idResena);

        return ResponseEntity.ok(resena);
    }

    /**
     * Reporta una reseña como inapropiada
     */
    @PostMapping("/{idResena}/reportar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR')")
    @Operation(
            summary = "Reportar reseña inapropiada",
            description = "Permite reportar una reseña que contenga contenido ofensivo, inapropiado o spam. La reseña será revisada por un administrador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reseña reportada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La reseña ya ha sido reportada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reseña no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> reportarResena(
            @Parameter(description = "ID de la reseña a reportar", required = true)
            @PathVariable String idResena) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/resenas/{}/reportar - Usuario: {}", idResena, idUsuario);

        MessageResponse response = resenaService.reportarResena(idResena, idUsuario);

        log.info("Reseña {} reportada por usuario {}", idResena, idUsuario);

        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una reseña
     * Solo el cliente que creó la reseña puede eliminarla
     */
    @DeleteMapping("/{idResena}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Eliminar reseña",
            description = "Permite al cliente eliminar su propia reseña. Esta acción es irreversible y actualizará la calificación promedio del servicio"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reseña eliminada exitosamente",
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
                    responseCode = "403",
                    description = "No autorizado - Solo el cliente dueño puede eliminar la reseña",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reseña no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> eliminarResena(
            @Parameter(description = "ID de la reseña a eliminar", required = true)
            @PathVariable String idResena) {

        String idCliente = obtenerIdUsuarioAutenticado();
        log.info("DELETE /api/v1/resenas/{} - Cliente: {}", idResena, idCliente);

        MessageResponse response = resenaService.eliminarResena(idResena, idCliente);

        log.info("Reseña {} eliminada por cliente {}", idResena, idCliente);

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
}