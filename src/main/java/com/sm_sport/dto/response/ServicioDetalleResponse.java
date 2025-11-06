package com.sm_sport.dto.response;

import com.sm_sport.model.enums.EstadoServicio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioDetalleResponse {

    private String idServicio;
    private String nombre;
    private String deporte;
    private String descripcion;
    private BigDecimal precio;
    private EstadoServicio estado;
    private LocalDateTime fechaPublicacion;
    private BigDecimal calificacionPromedio;
    private Integer totalResenas;

    // Proveedor completo
    private ProveedorResponse proveedor;

    // Ubicación completa
    private UbicacionResponse ubicacion;

    // Disponibilidades próximas
    private List<DisponibilidadResponse> disponibilidades;

    // Reseñas recientes
    private List<ResenaResponse> resenas;
}
