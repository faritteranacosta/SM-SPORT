package com.sm_sport.repository;

import com.sm_sport.model.entity.ReporteDesempeno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteDesempenoRepository extends JpaRepository<ReporteDesempeno, String> {

    // Reportes por proveedor
    List<ReporteDesempeno> findByProveedorIdUsuario(String idProveedor);

    @Query("SELECT r FROM ReporteDesempeno r WHERE r.proveedor.idUsuario = :idProveedor " +
            "ORDER BY r.fechaGeneracion DESC")
    List<ReporteDesempeno> findReportesByProveedorOrdenados(@Param("idProveedor") String idProveedor);

    // Último reporte de un proveedor
    @Query("SELECT r FROM ReporteDesempeno r WHERE r.proveedor.idUsuario = :idProveedor " +
            "ORDER BY r.fechaGeneracion DESC LIMIT 1")
    Optional<ReporteDesempeno> findUltimoReporteByProveedor(@Param("idProveedor") String idProveedor);

    // Reportes en rango de fechas
    @Query("SELECT r FROM ReporteDesempeno r WHERE r.fechaGeneracion BETWEEN :inicio AND :fin")
    List<ReporteDesempeno> findReportesEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Reportes por periodo específico
    @Query("SELECT r FROM ReporteDesempeno r WHERE r.fechaInicio = :fechaInicio " +
            "AND r.fechaFin = :fechaFin")
    List<ReporteDesempeno> findReportesByPeriodo(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );
}
