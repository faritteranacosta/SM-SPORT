package com.sm_sport.repository;

import com.sm_sport.model.entity.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, String> {

    // Búsqueda por verificación
    List<Proveedor> findByVerificado(Boolean verificado);

    // Proveedores con mejor calificación
    @Query("SELECT p FROM Proveedor p WHERE p.calificacionPromedio >= :calificacionMinima " +
            "ORDER BY p.calificacionPromedio DESC")
    List<Proveedor> findTopProveedores(@Param("calificacionMinima") BigDecimal calificacionMinima);

    // Proveedores con servicios activos
    @Query("SELECT DISTINCT p FROM Proveedor p JOIN p.serviciosPublicados s WHERE s.estado = 'PUBLICADO'")
    List<Proveedor> findProveedoresConServiciosActivos();

    // Proveedores por calificación (paginado)
    Page<Proveedor> findByCalificacionPromedioGreaterThanEqualOrderByCalificacionPromedioDesc(
            BigDecimal calificacion, Pageable pageable
    );

    // Estadísticas
    @Query("SELECT AVG(p.calificacionPromedio) FROM Proveedor p WHERE p.verificado = true")
    BigDecimal calcularCalificacionPromedioProveedoresVerificados();

    @Query("SELECT COUNT(p) FROM Proveedor p WHERE p.totalServiciosPublicados > 0")
    Long contarProveedoresActivos();
}
