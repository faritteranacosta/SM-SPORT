package com.sm_sport.controller;

import com.sm_sport.dto.response.EstadisticasResponse;
import com.sm_sport.dto.response.ReporteDesempenoResponse;
import com.sm_sport.service.ReporteService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestión de reportes y estadísticas
 * Permite a proveedores ver su desempeño y a administradores ver métricas globales
 */
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reportes", description = "Generación de reportes de desempeño y estadísticas del sistema")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteService reporteService;

    /**
     * Genera un reporte de desempeño para un proveedor en un periodo específico
     */
    @PostMapping("/generar")
    @PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Generar reporte de desempeño",
            description = "Genera un reporte detallado de desempeño para un proveedor en un rango de fechas específico, incluyendo ventas, ingresos, calificaciones y métricas clave"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reporte generado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReporteDesempenoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Fechas inválidas - La fecha fin debe ser posterior a la fecha inicio",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo puedes generar reportes de tu propia cuenta",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<ReporteDesempenoResponse> generarReporte(
            @Parameter(description = "ID del proveedor (opcional, se usa el autenticado si no se especifica)")
            @RequestParam(required = false) String idProveedor,

            @Parameter(
                    description = "Fecha de inicio del periodo (formato: yyyy-MM-dd)",
                    required = true,
                    example = "2024-01-01"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,

            @Parameter(
                    description = "Fecha de fin del periodo (formato: yyyy-MM-dd)",
                    required = true,
                    example = "2024-12-31"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        String idUsuarioAutenticado = obtenerIdUsuarioAutenticado();

        // Si no se especifica proveedor, usar el usuario autenticado
        String idProveedorFinal = idProveedor != null ? idProveedor : idUsuarioAutenticado;

        // Validar que sea el propio proveedor o un administrador
        if (!esAdministrador() && !idProveedorFinal.equals(idUsuarioAutenticado)) {
            log.warn("Usuario {} intentó generar reporte de proveedor {} sin permisos",
                    idUsuarioAutenticado, idProveedorFinal);
            throw new com.sm_sport.exception.ForbiddenException(
                    "No tienes permisos para generar reportes de otros proveedores");
        }

        log.info("POST /api/v1/reportes/generar - Proveedor: {} - Periodo: {} a {}",
                idProveedorFinal, fechaInicio, fechaFin);

        ReporteDesempenoResponse reporte = reporteService.generarReporteProveedor(
                idProveedorFinal, fechaInicio, fechaFin);

        log.info("Reporte generado exitosamente con ID: {} - Total ventas: {}",
                reporte.getIdReporte(), reporte.getTotalVentas());

        return ResponseEntity.status(HttpStatus.CREATED).body(reporte);
    }

    /**
     * Lista todos los reportes históricos de un proveedor
     */
    @GetMapping("/proveedor/{idProveedor}")
    @PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Listar reportes de un proveedor",
            description = "Obtiene el historial completo de reportes generados para un proveedor específico, ordenados por fecha de generación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de reportes obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReporteDesempenoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo puedes ver tus propios reportes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<List<ReporteDesempenoResponse>> listarReportesProveedor(
            @Parameter(description = "ID del proveedor", required = true)
            @PathVariable String idProveedor) {

        String idUsuarioAutenticado = obtenerIdUsuarioAutenticado();

        // Validar que sea el propio proveedor o un administrador
        if (!esAdministrador() && !idProveedor.equals(idUsuarioAutenticado)) {
            log.warn("Usuario {} intentó acceder a reportes de proveedor {} sin permisos",
                    idUsuarioAutenticado, idProveedor);
            throw new com.sm_sport.exception.ForbiddenException(
                    "No tienes permisos para ver reportes de otros proveedores");
        }

        log.info("GET /api/v1/reportes/proveedor/{} - Usuario: {}",
                idProveedor, idUsuarioAutenticado);

        List<ReporteDesempenoResponse> reportes = reporteService.listarReportes(idProveedor);

        log.info("Se encontraron {} reportes para el proveedor {}", reportes.size(), idProveedor);

        return ResponseEntity.ok(reportes);
    }

    /**
     * Lista los reportes del proveedor autenticado
     * Endpoint conveniente para que el proveedor vea sus propios reportes
     */
    @GetMapping("/mis-reportes")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Listar mis reportes",
            description = "Obtiene todos los reportes de desempeño del proveedor autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de reportes obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReporteDesempenoResponse.class)
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
                    description = "Proveedor no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<List<ReporteDesempenoResponse>> listarMisReportes() {
        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/reportes/mis-reportes - Proveedor: {}", idProveedor);

        List<ReporteDesempenoResponse> reportes = reporteService.listarReportes(idProveedor);

        log.info("Proveedor {} tiene {} reportes generados", idProveedor, reportes.size());

        return ResponseEntity.ok(reportes);
    }

    /**
     * Obtiene estadísticas generales del sistema
     * Solo administradores pueden acceder
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Obtener estadísticas generales del sistema",
            description = "[ADMIN] Obtiene métricas y estadísticas globales del sistema: usuarios, servicios, reservas, ingresos, calificaciones y denuncias"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EstadisticasResponse.class)
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
    public ResponseEntity<EstadisticasResponse> obtenerEstadisticasGenerales() {
        String idAdmin = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/reportes/estadisticas - Admin: {}", idAdmin);

        EstadisticasResponse estadisticas = reporteService.obtenerEstadisticasGenerales();

        log.info("Estadísticas generales obtenidas - Total usuarios: {}, Total servicios: {}, Total reservas: {}",
                estadisticas.getTotalUsuarios(),
                estadisticas.getTotalServicios(),
                estadisticas.getTotalReservas());

        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Genera reportes mensuales de forma manual
     * Solo administradores - Útil para ejecutar el proceso fuera del schedule automático
     */
    @PostMapping("/generar-mensuales")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Generar reportes mensuales manualmente",
            description = "[ADMIN] Ejecuta manualmente el proceso de generación de reportes mensuales para todos los proveedores activos. Normalmente se ejecuta automáticamente el día 1 de cada mes"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Proceso de generación iniciado exitosamente",
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
            )
    })
    public ResponseEntity<com.sm_sport.dto.response.MessageResponse> generarReportesMensuales() {
        String idAdmin = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/reportes/generar-mensuales - Admin: {}", idAdmin);

        reporteService.generarReportesMensuales();

        log.info("Proceso de generación de reportes mensuales completado por admin {}", idAdmin);

        return ResponseEntity.ok(
                com.sm_sport.dto.response.MessageResponse.success(
                        "Reportes mensuales generados exitosamente para todos los proveedores activos"
                )
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
}