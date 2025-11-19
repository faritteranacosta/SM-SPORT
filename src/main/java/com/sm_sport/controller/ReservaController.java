package com.sm_sport.controller;

import com.sm_sport.dto.request.CancelarReservaRequest;
import com.sm_sport.dto.request.CrearReservaRequest;
import com.sm_sport.dto.request.FiltroReservaRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PageResponse;
import com.sm_sport.dto.response.ReservaDetalleResponse;
import com.sm_sport.dto.response.ReservaResponse;
import com.sm_sport.service.ReservaService;
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

/**
 * Controlador REST para gestión de reservas deportivas
 * Permite crear, consultar, confirmar y cancelar reservas
 */
@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservas", description = "Gestión de reservas de servicios deportivos")
@SecurityRequirement(name = "bearerAuth")
public class ReservaController {

    private final ReservaService reservaService;

    /**
     * Crea una nueva reserva para un servicio
     * Solo clientes pueden crear reservas
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Crear reserva",
            description = "Permite a un cliente crear una reserva para un servicio deportivo. Valida disponibilidad, fecha futura y genera la reserva en estado PENDIENTE hasta que se realice el pago"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reserva creada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos - No hay disponibilidad o fecha inválida",
                    content = @Content
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
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<ReservaResponse> crearReserva(
            @Parameter(description = "Datos de la reserva: ID del servicio, fecha, hora y notas opcionales", required = true)
            @Valid @RequestBody CrearReservaRequest request) {

        String idCliente = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/reservas - Cliente: {} - Servicio: {} - Fecha: {}",
                idCliente, request.getIdServicio(), request.getFechaReserva());

        ReservaResponse reserva = reservaService.crearReserva(idCliente, request);

        log.info("Reserva creada exitosamente con ID: {} - Estado: {}",
                reserva.getIdReserva(), reserva.getEstado());

        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    /**
     * Lista todas las reservas del usuario autenticado
     * Los clientes ven sus reservas, los proveedores ven las reservas recibidas
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR')")
    @Operation(
            summary = "Listar mis reservas",
            description = "Obtiene las reservas del usuario autenticado. Si es CLIENTE: sus reservas realizadas. Si es PROVEEDOR: reservas recibidas en sus servicios"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de reservas obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            )
    })
    public ResponseEntity<PageResponse<ReservaResponse>> listarMisReservas(
            @Parameter(description = "Número de página (inicia en 0)")
            @RequestParam(defaultValue = "0") Integer pagina,

            @Parameter(description = "Cantidad de elementos por página")
            @RequestParam(defaultValue = "20") Integer tamano) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        boolean esCliente = esCliente();

        log.info("GET /api/v1/reservas - Usuario: {} - Rol: {} - Página: {}/{}",
                idUsuario, esCliente ? "CLIENTE" : "PROVEEDOR", pagina, tamano);

        PageResponse<ReservaResponse> reservas;

        if (esCliente) {
            reservas = reservaService.listarPorCliente(idUsuario, pagina, tamano);
        } else {
            reservas = reservaService.listarPorProveedor(idUsuario, pagina, tamano);
        }

        log.info("Se encontraron {} reservas para el usuario {}",
                reservas.getTotalElements(), idUsuario);

        return ResponseEntity.ok(reservas);
    }

    /**
     * Obtiene los detalles básicos de una reserva
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener reserva por ID",
            description = "Obtiene información básica de una reserva. Solo el cliente dueño, el proveedor del servicio o administradores pueden acceder"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserva encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - La reserva no te pertenece",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<ReservaResponse> obtenerReserva(
            @Parameter(description = "ID de la reserva", required = true)
            @PathVariable String id) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/reservas/{} - Usuario: {}", id, idUsuario);

        ReservaResponse reserva = reservaService.obtenerPorId(id);

        // Validar permisos (cliente dueño, proveedor o admin)
        if (!esAdministrador() &&
                !reserva.getIdCliente().equals(idUsuario) &&
                !reserva.getIdProveedor().equals(idUsuario)) {

            log.warn("Usuario {} intentó acceder a reserva {} sin permisos", idUsuario, id);
            throw new com.sm_sport.exception.ForbiddenException(
                    "No tienes permisos para ver esta reserva");
        }

        log.info("Reserva {} obtenida por usuario {}", id, idUsuario);

        return ResponseEntity.ok(reserva);
    }

    /**
     * Obtiene el detalle completo de una reserva
     * Incluye servicio, pago, reseña y solicitud de reembolso si existen
     */
    @GetMapping("/{id}/detalle")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener detalle completo de reserva",
            description = "Obtiene información detallada de la reserva incluyendo datos del servicio, pago realizado, reseña publicada y solicitud de reembolso si existe"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle de reserva obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservaDetalleResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - La reserva no te pertenece",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<ReservaDetalleResponse> obtenerDetalleReserva(
            @Parameter(description = "ID de la reserva", required = true)
            @PathVariable String id) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/reservas/{}/detalle - Usuario: {}", id, idUsuario);

        ReservaDetalleResponse detalle = reservaService.obtenerDetalle(id);

        // ======================== VALIDACIÓN DE PERMISOS ========================
        boolean esClienteDueno = detalle.getCliente().getIdUsuario().equals(idUsuario);
        boolean esProveedorDueno = detalle.getServicio()
                .getProveedor()
                .getIdUsuario()
                .equals(idUsuario);

        if (!esAdministrador() && !esClienteDueno && !esProveedorDueno) {
            throw new com.sm_sport.exception.ForbiddenException(
                    "No tienes permisos para ver esta reserva");
        }
        // ========================================================================

        log.info("Detalle de reserva {} obtenido exitosamente", id);

        return ResponseEntity.ok(detalle);
    }

    /**
     * Confirma una reserva (solo proveedor)
     */
    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Confirmar reserva",
            description = "Permite al proveedor dueño del servicio confirmar una reserva en estado PENDIENTE. El cliente será notificado de la confirmación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserva confirmada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solo se pueden confirmar reservas pendientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo el proveedor dueño del servicio",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<ReservaResponse> confirmarReserva(
            @Parameter(description = "ID de la reserva a confirmar", required = true)
            @PathVariable String id) {

        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/reservas/{}/confirmar - Proveedor: {}", id, idProveedor);

        ReservaResponse reserva = reservaService.confirmarReserva(id, idProveedor);

        log.info("Reserva {} confirmada por proveedor {}", id, idProveedor);

        return ResponseEntity.ok(reserva);
    }

    /**
     * Rechaza una reserva (solo proveedor)
     */
    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Rechazar reserva",
            description = "Permite al proveedor rechazar una reserva proporcionando un motivo. La disponibilidad será restaurada y el cliente será notificado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserva rechazada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo el proveedor dueño del servicio",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<ReservaResponse> rechazarReserva(
            @Parameter(description = "ID de la reserva a rechazar", required = true)
            @PathVariable String id,

            @Parameter(description = "Motivo del rechazo", required = true)
            @RequestParam String motivo) {

        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/reservas/{}/rechazar - Proveedor: {} - Motivo: {}",
                id, idProveedor, motivo);

        ReservaResponse reserva = reservaService.rechazarReserva(id, idProveedor, motivo);

        log.info("Reserva {} rechazada por proveedor {}", id, idProveedor);

        return ResponseEntity.ok(reserva);
    }

    /**
     * Cancela una reserva (solo cliente)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Cancelar reserva",
            description = "Permite al cliente cancelar su reserva proporcionando un motivo. Dependiendo de las políticas de reembolso, puede solicitar devolución del pago"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserva cancelada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede cancelar una reserva finalizada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo el cliente dueño puede cancelar",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> cancelarReserva(
            @Parameter(description = "ID de la reserva a cancelar", required = true)
            @PathVariable String id,

            @Parameter(description = "Motivo de la cancelación", required = true)
            @Valid @RequestBody CancelarReservaRequest request) {

        String idCliente = obtenerIdUsuarioAutenticado();
        log.info("DELETE /api/v1/reservas/{} - Cliente: {} - Motivo: {}",
                id, idCliente, request.getMotivoCancelacion());

        MessageResponse response = reservaService.cancelarReserva(id, idCliente, request);

        log.info("Reserva {} cancelada por cliente {}", id, idCliente);

        return ResponseEntity.ok(response);
    }

    /**
     * Finaliza una reserva
     * Puede ser llamado por el proveedor o automáticamente por el sistema
     */
    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Finalizar reserva",
            description = "Marca una reserva como finalizada después de que el servicio fue consumido. Permite al cliente calificar el servicio"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reserva finalizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo proveedores",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva no encontrada",
                    content = @Content
            )
    })
    public ResponseEntity<ReservaResponse> finalizarReserva(
            @Parameter(description = "ID de la reserva a finalizar", required = true)
            @PathVariable String id) {

        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/reservas/{}/finalizar - Proveedor: {}", id, idProveedor);

        ReservaResponse reserva = reservaService.finalizarReserva(id);

        log.info("Reserva {} finalizada exitosamente", id);

        return ResponseEntity.ok(reserva);
    }

    /**
     * Busca reservas con filtros avanzados
     */
    @PostMapping("/buscar")
    @PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Buscar reservas con filtros",
            description = "Busca reservas aplicando múltiples filtros: estados, fechas, cliente, proveedor o servicio. Incluye paginación y ordenamiento"
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
                    description = "No autorizado - Solo proveedores y administradores",
                    content = @Content
            )
    })
    public ResponseEntity<PageResponse<ReservaResponse>> buscarReservas(
            @Parameter(description = "Filtros de búsqueda", required = true)
            @Valid @RequestBody FiltroReservaRequest filtros) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/reservas/buscar - Usuario: {} - Filtros: {}", idUsuario, filtros);

        PageResponse<ReservaResponse> reservas = reservaService.filtrarReservas(filtros);

        log.info("Búsqueda completada: {} reservas encontradas", reservas.getTotalElements());

        return ResponseEntity.ok(reservas);
    }

    /**
     * Verifica disponibilidad para una reserva sin crearla
     */
    @PostMapping("/verificar-disponibilidad")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Verificar disponibilidad",
            description = "Verifica si hay disponibilidad para una reserva en la fecha y hora especificadas sin crear la reserva"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidad verificada",
                    content = @Content
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
    public ResponseEntity<MessageResponse> verificarDisponibilidad(
            @Parameter(description = "Datos de la reserva a verificar", required = true)
            @Valid @RequestBody CrearReservaRequest request) {

        String idCliente = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/reservas/verificar-disponibilidad - Cliente: {} - Servicio: {}",
                idCliente, request.getIdServicio());

        boolean disponible = reservaService.verificarDisponibilidad(request.getIdServicio(), request);

        String mensaje = disponible ?
                "El servicio está disponible para la fecha y hora seleccionadas" :
                "No hay disponibilidad para la fecha y hora seleccionadas";

        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message(mensaje)
                        .success(disponible)
                        .timestamp(java.time.LocalDateTime.now())
                        .build()
        );
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

    /**
     * Verifica si el usuario autenticado es cliente
     */
    private boolean esCliente() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE"));
    }
}
