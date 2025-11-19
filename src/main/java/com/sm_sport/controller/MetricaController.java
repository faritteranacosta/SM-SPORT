package com.sm_sport.controller;

import com.sm_sport.dto.response.KPIResponse;
import com.sm_sport.dto.response.MetricaResponse;
import com.sm_sport.service.MetricaService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/metricas")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin - Métricas y KPIs", description = "Endpoints para consultar métricas del sistema y KPIs (Solo Administradores)")
@SecurityRequirement(name = "Bearer Authentication")
public class MetricaController {

    private final MetricaService metricaService;

    /**
     * Obtiene todas las métricas de una categoría específica
     * GET /api/v1/admin/metricas/categoria/{categoria}
     */
    @Operation(
            summary = "Obtener métricas por categoría",
            description = "Retorna todas las métricas registradas de una categoría específica (USUARIOS, RESERVAS, INGRESOS, SERVICIOS)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Métricas obtenidas exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MetricaResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Se requiere rol ADMINISTRADOR",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontraron métricas para la categoría especificada",
                    content = @Content
            )
    })
    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<MetricaResponse>> obtenerMetricasPorCategoria(
            @Parameter(
                    description = "Categoría de métricas a consultar",
                    example = "USUARIOS",
                    required = true
            )
            @PathVariable String categoria
    ) {
        log.info("GET /api/v1/admin/metricas/categoria/{} - Obteniendo métricas", categoria);

        List<MetricaResponse> metricas = metricaService.obtenerPorCategoria(categoria.toUpperCase());

        if (metricas.isEmpty()) {
            log.warn("No se encontraron métricas para la categoría: {}", categoria);
        } else {
            log.info("Se encontraron {} métricas para la categoría: {}", metricas.size(), categoria);
        }

        return ResponseEntity.ok(metricas);
    }

    /**
     * Calcula y retorna todos los KPIs del sistema
     * GET /api/v1/admin/metricas/kpis
     */
    @Operation(
            summary = "Calcular y obtener KPIs del sistema",
            description = "Calcula y retorna todos los Indicadores Clave de Rendimiento (KPIs) del sistema: " +
                    "Usuarios Activos, Tasa de Conversión, Ingreso Promedio, Servicios Publicados y Tasa de Ocupación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "KPIs calculados exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KPIResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Se requiere rol ADMINISTRADOR",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al calcular KPIs",
                    content = @Content
            )
    })
    @GetMapping("/kpis")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<KPIResponse>> calcularKPIs() {
        log.info("GET /api/v1/admin/metricas/kpis - Calculando KPIs del sistema");

        List<KPIResponse> kpis = metricaService.calcularKPIs();

        log.info("Se calcularon {} KPIs exitosamente", kpis.size());

        return ResponseEntity.ok(kpis);
    }

    /**
     * Genera manualmente las métricas diarias del sistema
     * POST /api/v1/admin/metricas/generar-diarias
     */
    @Operation(
            summary = "Generar métricas diarias manualmente",
            description = "Ejecuta manualmente el proceso de generación de métricas diarias. " +
                    "Normalmente este proceso se ejecuta automáticamente a la 1:00 AM, " +
                    "pero puede ser ejecutado manualmente cuando sea necesario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Métricas diarias generadas exitosamente",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Se requiere rol ADMINISTRADOR",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al generar métricas",
                    content = @Content
            )
    })
    @PostMapping("/generar-diarias")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<String> generarMetricasDiarias() {
        log.info("POST /api/v1/admin/metricas/generar-diarias - Generación manual de métricas solicitada");

        try {
            metricaService.generarMetricasDiarias();
            log.info("Métricas diarias generadas manualmente de forma exitosa");
            return ResponseEntity.ok("Métricas diarias generadas exitosamente");
        } catch (Exception e) {
            log.error("Error al generar métricas diarias manualmente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar métricas: " + e.getMessage());
        }
    }

    /**
     * Registra una métrica personalizada manualmente
     * POST /api/v1/admin/metricas
     */
    @Operation(
            summary = "Registrar métrica personalizada",
            description = "Permite registrar manualmente una métrica personalizada en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Métrica registrada exitosamente",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Se requiere rol ADMINISTRADOR",
                    content = @Content
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<String> registrarMetrica(
            @Parameter(description = "Nombre de la métrica", example = "Usuarios Nuevos", required = true)
            @RequestParam String nombre,

            @Parameter(description = "Valor numérico de la métrica", example = "150.5", required = true)
            @RequestParam Double valor,

            @Parameter(description = "Unidad de medida", example = "usuarios", required = true)
            @RequestParam String unidad,

            @Parameter(description = "Categoría de la métrica", example = "USUARIOS", required = true)
            @RequestParam String categoria
    ) {
        log.info("POST /api/v1/admin/metricas - Registrando métrica: {} = {} {}", nombre, valor, unidad);

        metricaService.registrarMetrica(nombre, valor, unidad, categoria.toUpperCase());

        log.info("Métrica registrada exitosamente: {}", nombre);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Métrica '" + nombre + "' registrada exitosamente");
    }
}