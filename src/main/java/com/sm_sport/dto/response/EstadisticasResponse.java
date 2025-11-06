package com.sm_sport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasResponse {

    // Estadísticas de usuarios
    private Long totalUsuarios;
    private Long usuariosActivos;
    private Long totalClientes;
    private Long totalProveedores;
    private Map<String, Long> usuariosPorRol;

    // Estadísticas de servicios
    private Long totalServicios;
    private Long serviciosPublicados;
    private Map<String, Long> serviciosPorDeporte;
    private Map<String, Long> serviciosPorCiudad;

    // Estadísticas de reservas
    private Long totalReservas;
    private Long reservasConfirmadas;
    private Long reservasCanceladas;
    private Long reservasFinalizadas;
    private BigDecimal ingresosGenerados;

    // Estadísticas de calidad
    private BigDecimal calificacionPromedio;
    private Long totalResenas;

    // Estadísticas de denuncias
    private Long denunciasPendientes;
    private Long denunciasAtendidas;
}
