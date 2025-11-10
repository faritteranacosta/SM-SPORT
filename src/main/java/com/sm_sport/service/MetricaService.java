package com.sm_sport.service;

import com.sm_sport.dto.response.KPIResponse;
import com.sm_sport.dto.response.MetricaResponse;

import java.util.List;

public interface MetricaService {

    /**
     * Registra una métrica del sistema
     *
     * @param nombre    Nombre de la métrica
     * @param valor     Valor de la métrica
     * @param unidad    Unidad de medida
     * @param categoria Categoría
     */
    void registrarMetrica(String nombre, Double valor, String unidad, String categoria);

    /**
     * Obtiene métricas por categoría
     *
     * @param categoria Categoría
     * @return Lista de métricas
     */
    List<MetricaResponse> obtenerPorCategoria(String categoria);

    /**
     * Calcula KPIs del sistema
     *
     * @return Lista de KPIs
     */
    List<KPIResponse> calcularKPIs();

    /**
     * Genera métricas automáticas diarias
     */
    void generarMetricasDiarias();
}
