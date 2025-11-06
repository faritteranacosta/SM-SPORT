package com.sm_sport.repository;

import com.sm_sport.model.entity.MetricaSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MetricaSistemaRepository extends JpaRepository<MetricaSistema, String> {

    // Métricas por nombre
    List<MetricaSistema> findByNombreMetrica(String nombreMetrica);

    // Métricas por categoría
    List<MetricaSistema> findByCategoria(String categoria);

    // Métricas por periodo
    List<MetricaSistema> findByPeriodo(String periodo);

    // Última métrica por nombre
    @Query("SELECT m FROM MetricaSistema m WHERE m.nombreMetrica = :nombre " +
            "ORDER BY m.fechaMedicion DESC LIMIT 1")
    Optional<MetricaSistema> findUltimaMetrica(@Param("nombre") String nombre);

    // Métricas en rango de fechas
    @Query("SELECT m FROM MetricaSistema m WHERE m.fechaMedicion BETWEEN :inicio AND :fin " +
            "ORDER BY m.fechaMedicion ASC")
    List<MetricaSistema> findMetricasEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Métricas por categoría y periodo
    @Query("SELECT m FROM MetricaSistema m WHERE m.categoria = :categoria " +
            "AND m.periodo = :periodo ORDER BY m.fechaMedicion DESC")
    List<MetricaSistema> findMetricasByCategoriaAndPeriodo(
            @Param("categoria") String categoria,
            @Param("periodo") String periodo
    );
}
