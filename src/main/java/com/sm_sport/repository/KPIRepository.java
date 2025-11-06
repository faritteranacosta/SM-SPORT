package com.sm_sport.repository;

import com.sm_sport.model.entity.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KPIRepository extends JpaRepository<KPI, String> {

    // KPIs por nombre
    List<KPI> findByNombreKpi(String nombreKpi);

    // KPIs por periodo
    List<KPI> findByPeriodo(String periodo);

    // KPIs por tendencia
    List<KPI> findByTendencia(String tendencia);

    // Último KPI por nombre
    @Query("SELECT k FROM KPI k WHERE k.nombreKpi = :nombre " +
            "ORDER BY k.fechaCalculo DESC LIMIT 1")
    Optional<KPI> findUltimoKPI(@Param("nombre") String nombre);

    // KPIs en rango de fechas
    @Query("SELECT k FROM KPI k WHERE k.fechaCalculo BETWEEN :inicio AND :fin " +
            "ORDER BY k.fechaCalculo ASC")
    List<KPI> findKPIsEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // KPIs que no cumplieron meta
    @Query("SELECT k FROM KPI k WHERE k.porcentajeCumplimiento < 100 " +
            "ORDER BY k.porcentajeCumplimiento ASC")
    List<KPI> findKPIsNoCumplidos();

    // Todos los KPIs más recientes
    @Query("SELECT k FROM KPI k WHERE k.fechaCalculo = " +
            "(SELECT MAX(k2.fechaCalculo) FROM KPI k2 WHERE k2.nombreKpi = k.nombreKpi)")
    List<KPI> findKPIsMasRecientes();
}
