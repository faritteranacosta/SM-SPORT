package com.sm_sport.dto.response;

import com.sm_sport.model.enums.EstadoServicio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioResponse {

    private String idServicio;
    private String nombre;
    private String deporte;
    private String descripcion;
    private BigDecimal precio;
    private EstadoServicio estado;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaActualizacion;
    private BigDecimal calificacionPromedio;
    private Integer totalResenas;

    // Información del proveedor
    private String idProveedor;
    private String nombreProveedor;
    private Boolean proveedorVerificado;

    // Ubicación básica
    private String ciudad;
    private String departamento;
    private String direccion;
}
