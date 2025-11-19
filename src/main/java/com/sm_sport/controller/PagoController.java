package com.sm_sport.controller;

import com.sm_sport.dto.request.PagoRequest;
import com.sm_sport.dto.response.ComprobanteResponse;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PagoResponse;
import com.sm_sport.service.PagoService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Pagos", description = "Endpoints para procesamiento de pagos, comprobantes y reembolsos")
@SecurityRequirement(name = "Bearer Authentication")
public class PagoController {

    private final PagoService pagoService;

    /**
     * Procesar un nuevo pago para una reserva
     * POST /api/v1/pagos
     */
    @Operation(
            summary = "Procesar pago",
            description = "Procesa el pago de una reserva existente. La reserva debe estar en estado PENDIENTE. " +
                    "Al aprobar el pago, la reserva pasa a estado CONFIRMADA y se genera un comprobante automáticamente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pago procesado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PagoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de pago inválidos o reserva no elegible para pago",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "402",
                    description = "Pago rechazado por la pasarela de pagos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - La reserva no pertenece al cliente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reserva o cliente no encontrado",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PagoResponse> procesarPago(
            @Valid @RequestBody PagoRequest request
    ) {
        String idCliente = obtenerIdUsuarioAutenticado();

        log.info("POST /api/v1/pagos - Cliente {} procesando pago para reserva {}",
                idCliente, request.getIdReserva());

        PagoResponse response = pagoService.procesarPago(idCliente, request);

        log.info("Pago procesado exitosamente: {} - Estado: {}",
                response.getIdPago(), response.getEstadoPago());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener información de un pago por su ID
     * GET /api/v1/pagos/{id}
     */
    @Operation(
            summary = "Obtener información de pago",
            description = "Retorna los detalles completos de un pago específico por su ID. " +
                    "Los clientes solo pueden ver sus propios pagos, los administradores pueden ver todos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PagoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - El pago no pertenece al cliente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pago no encontrado",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMINISTRADOR')")
    public ResponseEntity<PagoResponse> obtenerPagoPorId(
            @Parameter(description = "ID del pago", required = true)
            @PathVariable String id
    ) {
        log.info("GET /api/v1/pagos/{} - Obteniendo información del pago", id);

        PagoResponse response = pagoService.obtenerPorId(id);

        log.info("Pago encontrado: {} - Estado: {}", response.getIdPago(), response.getEstadoPago());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener el pago asociado a una reserva específica
     * GET /api/v1/pagos/reserva/{idReserva}
     */
    @Operation(
            summary = "Obtener pago por reserva",
            description = "Retorna la información del pago asociado a una reserva específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PagoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró pago para la reserva especificada",
                    content = @Content
            )
    })
    @GetMapping("/reserva/{idReserva}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    public ResponseEntity<PagoResponse> obtenerPagoPorReserva(
            @Parameter(description = "ID de la reserva", required = true)
            @PathVariable String idReserva
    ) {
        log.info("GET /api/v1/pagos/reserva/{} - Obteniendo pago de reserva", idReserva);

        PagoResponse response = pagoService.obtenerPorReserva(idReserva);

        log.info("Pago encontrado para reserva {}: {}", idReserva, response.getIdPago());

        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los pagos del cliente autenticado
     * GET /api/v1/pagos/mis-pagos
     */
    @Operation(
            summary = "Listar mis pagos",
            description = "Retorna el historial completo de pagos realizados por el cliente autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pagos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PagoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            )
    })
    @GetMapping("/mis-pagos")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<PagoResponse>> listarMisPagos() {
        String idCliente = obtenerIdUsuarioAutenticado();

        log.info("GET /api/v1/pagos/mis-pagos - Cliente {} listando sus pagos", idCliente);

        List<PagoResponse> pagos = pagoService.listarPorCliente(idCliente);

        log.info("Se encontraron {} pagos para el cliente {}", pagos.size(), idCliente);

        return ResponseEntity.ok(pagos);
    }

    /**
     * Obtener/Generar el comprobante de un pago
     * GET /api/v1/pagos/{id}/comprobante
     */
    @Operation(
            summary = "Descargar comprobante de pago",
            description = "Genera y retorna el comprobante de pago en formato PDF. " +
                    "Si el comprobante ya existe, retorna el existente. " +
                    "Solo se pueden generar comprobantes de pagos aprobados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comprobante generado/obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ComprobanteResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El pago no está aprobado, no se puede generar comprobante",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pago no encontrado",
                    content = @Content
            )
    })
    @GetMapping("/{id}/comprobante")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    public ResponseEntity<ComprobanteResponse> obtenerComprobante(
            @Parameter(description = "ID del pago", required = true)
            @PathVariable String id
    ) {
        log.info("GET /api/v1/pagos/{}/comprobante - Generando/obteniendo comprobante", id);

        ComprobanteResponse comprobante = pagoService.generarComprobante(id);

        log.info("Comprobante generado/obtenido: {} para pago {}",
                comprobante.getIdComprobante(), id);

        return ResponseEntity.ok(comprobante);
    }

    /**
     * Procesar reembolso de un pago
     * POST /api/v1/pagos/{id}/reembolso
     */
    @Operation(
            summary = "Procesar reembolso",
            description = "Procesa el reembolso de un pago aprobado. " +
                    "Solo los administradores pueden procesar reembolsos. " +
                    "El pago debe estar en estado APROBADO para ser reembolsado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reembolso procesado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El pago no está en estado válido para reembolso",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "402",
                    description = "Error al procesar el reembolso con la pasarela",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Se requiere rol ADMINISTRADOR",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pago no encontrado",
                    content = @Content
            )
    })
    @PostMapping("/{id}/reembolso")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MessageResponse> procesarReembolso(
            @Parameter(description = "ID del pago a reembolsar", required = true)
            @PathVariable String id
    ) {
        log.info("POST /api/v1/pagos/{}/reembolso - Procesando reembolso", id);

        MessageResponse response = pagoService.procesarReembolso(id);

        log.info("Reembolso procesado exitosamente para pago: {}", id);

        return ResponseEntity.ok(response);
    }

    // ==================== MÉTODO AUXILIAR ====================

    /**
     * Obtiene el ID del usuario autenticado desde el contexto de seguridad
     */
    private String obtenerIdUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Asume que el username es el ID del usuario
    }
}