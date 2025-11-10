package com.sm_sport.service;

import com.sm_sport.dto.response.EstadisticasResponse;
import com.sm_sport.dto.response.ReporteDesempenoResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReporteService {

    /**
     * Genera reporte de desempeño para un proveedor
     *
     * @param idProveedor ID del proveedor
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Reporte generado
     */
    ReporteDesempenoResponse generarReporteProveedor(String idProveedor, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Lista reportes de un proveedor
     *
     * @param idProveedor ID del proveedor
     * @return Lista de reportes
     */
    List<ReporteDesempenoResponse> listarReportes(String idProveedor);

    /**
     * Obtiene estadísticas generales del sistema
     *
     * @return Estadísticas del sistema
     */
    EstadisticasResponse obtenerEstadisticasGenerales();

    /**
     * Genera reportes automáticos mensuales
     */
    void generarReportesMensuales();
}
