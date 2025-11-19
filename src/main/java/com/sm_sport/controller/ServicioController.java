package com.sm_sport.controller;

import com.sm_sport.dto.request.ActualizarServicioRequest;
import com.sm_sport.dto.request.BusquedaServicioRequest;
import com.sm_sport.dto.request.CrearServicioRequest;
import com.sm_sport.dto.request.DisponibilidadRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PageResponse;
import com.sm_sport.dto.response.ServicioDetalleResponse;
import com.sm_sport.dto.response.ServicioResponse;
import com.sm_sport.model.enums.EstadoServicio;
import com.sm_sport.service.ServicioService;
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
 * Controlador REST para gestión de servicios deportivos
 * Permite publicar, buscar, actualizar y gestionar servicios
 */
@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Servicios", description = "Gestión de servicios deportivos (clases, arriendos, eventos)")
@SecurityRequirement(name = "bearerAuth")
public class ServicioController {

    private final ServicioService servicioService;

    /**
     * Publica un nuevo servicio deportivo
     * Solo proveedores pueden publicar servicios
     */
    @PostMapping
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Publicar servicio",
            description = "Permite a un proveedor publicar un nuevo servicio deportivo: clases de aprendizaje, arriendo de espacios o eventos deportivos. Incluye ubicación y disponibilidad"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Servicio publicado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicioResponse.class)
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
    public ResponseEntity<ServicioResponse> publicarServicio(
            @Parameter(description = "Datos del servicio: nombre, deporte, precio, ubicación y disponibilidad", required = true)
            @Valid @RequestBody CrearServicioRequest request) {

        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/servicios - Proveedor: {} - Servicio: {}",
                idProveedor, request.getNombre());

        ServicioResponse servicio = servicioService.publicarServicio(idProveedor, request);

        log.info("Servicio publicado exitosamente con ID: {}", servicio.getIdServicio());

        return ResponseEntity.status(HttpStatus.CREATED).body(servicio);
    }

    /**
     * Lista todos los servicios publicados con paginación
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Listar servicios",
            description = "Obtiene un listado paginado de todos los servicios deportivos publicados y activos en Santa Marta"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de servicios obtenida exitosamente",
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
    public ResponseEntity<PageResponse<ServicioResponse>> listarServicios(
            @Parameter(description = "Número de página (inicia en 0)")
            @RequestParam(defaultValue = "0") Integer pagina,

            @Parameter(description = "Cantidad de elementos por página")
            @RequestParam(defaultValue = "20") Integer tamano) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/servicios - Usuario: {} - Página: {}/{}", idUsuario, pagina, tamano);

        PageResponse<ServicioResponse> servicios = servicioService.listarServicios(pagina, tamano);

        log.info("Se encontraron {} servicios publicados", servicios.getTotalElements());

        return ResponseEntity.ok(servicios);
    }

    /**
     * Obtiene los detalles básicos de un servicio
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener servicio por ID",
            description = "Obtiene la información básica de un servicio específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicioResponse.class)
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
    public ResponseEntity<ServicioResponse> obtenerServicio(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String id) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/servicios/{} - Usuario: {}", id, idUsuario);

        ServicioResponse servicio = servicioService.obtenerPorId(id);

        log.info("Servicio {} obtenido exitosamente", id);

        return ResponseEntity.ok(servicio);
    }

    /**
     * Obtiene el detalle completo de un servicio
     * Incluye información del proveedor, ubicación, disponibilidad y reseñas
     */
    @GetMapping("/{id}/detalle")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener detalle completo del servicio",
            description = "Obtiene información detallada del servicio incluyendo datos del proveedor, ubicación exacta, disponibilidad próxima y reseñas recientes"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle del servicio obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicioDetalleResponse.class)
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
    public ResponseEntity<ServicioDetalleResponse> obtenerDetalleServicio(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String id) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/servicios/{}/detalle - Usuario: {}", id, idUsuario);

        ServicioDetalleResponse detalle = servicioService.obtenerDetalle(id);

        log.info("Detalle del servicio {} obtenido exitosamente", id);

        return ResponseEntity.ok(detalle);
    }

    /**
     * Actualiza un servicio existente
     * Solo el proveedor dueño puede actualizar
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Actualizar servicio",
            description = "Permite al proveedor dueño actualizar la información de su servicio: nombre, descripción, precio, ubicación. Solo actualiza los campos enviados"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicioResponse.class)
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
                    responseCode = "403",
                    description = "No autorizado - Solo el proveedor dueño puede actualizar",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<ServicioResponse> actualizarServicio(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String id,

            @Parameter(description = "Datos a actualizar", required = true)
            @Valid @RequestBody ActualizarServicioRequest request) {

        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("PUT /api/v1/servicios/{} - Proveedor: {}", id, idProveedor);

        ServicioResponse servicio = servicioService.actualizarServicio(id, request);

        log.info("Servicio {} actualizado exitosamente", id);

        return ResponseEntity.ok(servicio);
    }

    /**
     * Elimina un servicio (soft delete)
     * Solo el proveedor dueño puede eliminar
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Eliminar servicio",
            description = "Desactiva un servicio (soft delete). No se puede eliminar si tiene reservas activas. El servicio se marca como ELIMINADO pero se mantiene en la base de datos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio eliminado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El servicio tiene reservas activas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo el proveedor dueño puede eliminar",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> eliminarServicio(
            @Parameter(description = "ID del servicio a eliminar", required = true)
            @PathVariable String id) {

        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("DELETE /api/v1/servicios/{} - Proveedor: {}", id, idProveedor);

        MessageResponse response = servicioService.eliminarServicio(id);

        log.info("Servicio {} eliminado por proveedor {}", id, idProveedor);

        return ResponseEntity.ok(response);
    }

    /**
     * Busca servicios con filtros avanzados
     */
    @PostMapping("/buscar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Búsqueda avanzada de servicios",
            description = "Busca servicios aplicando múltiples filtros: deporte, ciudad, rango de precios, calificación mínima. Incluye paginación y ordenamiento personalizado"
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
            )
    })
    public ResponseEntity<PageResponse<ServicioResponse>> buscarServicios(
            @Parameter(description = "Filtros de búsqueda", required = true)
            @Valid @RequestBody BusquedaServicioRequest filtros) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/servicios/buscar - Usuario: {} - Filtros: {}", idUsuario, filtros);

        PageResponse<ServicioResponse> servicios = servicioService.buscarServicios(filtros);

        log.info("Búsqueda completada: {} servicios encontrados", servicios.getTotalElements());

        return ResponseEntity.ok(servicios);
    }

    /**
     * Busca servicios cercanos a una ubicación
     */
    @GetMapping("/cercanos")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Buscar servicios cercanos",
            description = "Encuentra servicios deportivos cercanos a una ubicación geográfica específica dentro de un radio en kilómetros"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicios cercanos encontrados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicioResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            )
    })
    public ResponseEntity<List<ServicioResponse>> buscarServiciosCercanos(
            @Parameter(description = "Latitud de la ubicación", required = true, example = "11.0041")
            @RequestParam Double latitud,

            @Parameter(description = "Longitud de la ubicación", required = true, example = "-74.8070")
            @RequestParam Double longitud,

            @Parameter(description = "Radio de búsqueda en kilómetros", example = "5")
            @RequestParam(defaultValue = "5") Integer radioKm) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/servicios/cercanos - Usuario: {} - Ubicación: {},{} - Radio: {}km",
                idUsuario, latitud, longitud, radioKm);

        List<ServicioResponse> servicios = servicioService.buscarServiciosCercanos(latitud, longitud, radioKm);

        log.info("Se encontraron {} servicios cercanos", servicios.size());

        return ResponseEntity.ok(servicios);
    }

    /**
     * Lista los servicios publicados por un proveedor
     */
    @GetMapping("/proveedor/{idProveedor}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PROVEEDOR', 'ADMINISTRADOR')")
    @Operation(
            summary = "Listar servicios de un proveedor",
            description = "Obtiene todos los servicios publicados por un proveedor específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de servicios obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicioResponse.class)
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
    public ResponseEntity<List<ServicioResponse>> listarServiciosProveedor(
            @Parameter(description = "ID del proveedor", required = true)
            @PathVariable String idProveedor) {

        String idUsuario = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/servicios/proveedor/{} - Usuario: {}", idProveedor, idUsuario);

        List<ServicioResponse> servicios = servicioService.listarPorProveedor(idProveedor);

        log.info("Proveedor {} tiene {} servicios publicados", idProveedor, servicios.size());

        return ResponseEntity.ok(servicios);
    }

    /**
     * Lista los servicios del proveedor autenticado
     */
    @GetMapping("/mis-servicios")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Listar mis servicios",
            description = "Obtiene todos los servicios publicados por el proveedor autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de servicios obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicioResponse.class)
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
            )
    })
    public ResponseEntity<List<ServicioResponse>> listarMisServicios() {
        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("GET /api/v1/servicios/mis-servicios - Proveedor: {}", idProveedor);

        List<ServicioResponse> servicios = servicioService.listarPorProveedor(idProveedor);

        log.info("Proveedor {} tiene {} servicios publicados", idProveedor, servicios.size());

        return ResponseEntity.ok(servicios);
    }

    /**
     * Agrega disponibilidad a un servicio
     */
    @PostMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Agregar disponibilidad",
            description = "Permite al proveedor dueño agregar horarios disponibles para reservar el servicio (días, horas, cupos)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Disponibilidad agregada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class)
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
                    responseCode = "403",
                    description = "No autorizado - Solo el proveedor dueño",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<MessageResponse> agregarDisponibilidad(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String id,

            @Parameter(description = "Lista de disponibilidades a agregar", required = true)
            @Valid @RequestBody List<DisponibilidadRequest> disponibilidades) {

        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("POST /api/v1/servicios/{}/disponibilidad - Proveedor: {} - Cantidad: {}",
                id, idProveedor, disponibilidades.size());

        MessageResponse response = servicioService.agregarDisponibilidad(id, disponibilidades);

        log.info("Disponibilidad agregada exitosamente para servicio {}", id);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cambia el estado de un servicio
     * Solo el proveedor dueño puede cambiar el estado
     */
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Cambiar estado del servicio",
            description = "Permite al proveedor cambiar el estado de su servicio (PUBLICADO, PAUSADO, ELIMINADO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicioResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token inválido o ausente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Solo el proveedor dueño",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Servicio no encontrado",
                    content = @Content
            )
    })
    public ResponseEntity<ServicioResponse> cambiarEstado(
            @Parameter(description = "ID del servicio", required = true)
            @PathVariable String id,

            @Parameter(description = "Nuevo estado del servicio", required = true)
            @RequestParam EstadoServicio estado) {

        String idProveedor = obtenerIdUsuarioAutenticado();
        log.info("PUT /api/v1/servicios/{}/estado - Proveedor: {} - Nuevo estado: {}",
                id, idProveedor, estado);

        ServicioResponse servicio = servicioService.cambiarEstado(id, estado);

        log.info("Estado del servicio {} cambiado a {}", id, estado);

        return ResponseEntity.ok(servicio);
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