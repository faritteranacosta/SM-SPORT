package com.sm_sport.repository;

import com.sm_sport.model.entity.Servicio;
import com.sm_sport.model.enums.EstadoServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, String> {

    // Búsqueda básica
    List<Servicio> findByEstado(EstadoServicio estado);

    List<Servicio> findByDeporte(String deporte);

    Page<Servicio> findByEstado(EstadoServicio estado, Pageable pageable);

    // Servicios por proveedor
    List<Servicio> findByProveedorIdUsuario(String idProveedor);

    @Query("SELECT s FROM Servicio s WHERE s.proveedor.idUsuario = :idProveedor AND s.estado = :estado")
    List<Servicio> findByProveedorAndEstado(
            @Param("idProveedor") String idProveedor,
            @Param("estado") EstadoServicio estado
    );

    // Búsqueda por deporte y estado
    List<Servicio> findByDeporteAndEstado(String deporte, EstadoServicio estado);

    Page<Servicio> findByDeporteAndEstado(String deporte, EstadoServicio estado, Pageable pageable);

    // Búsqueda por rango de precios
    @Query("SELECT s FROM Servicio s WHERE s.estado = :estado " +
            "AND s.precio BETWEEN :precioMin AND :precioMax " +
            "ORDER BY s.precio ASC")
    List<Servicio> buscarPorRangoPrecio(
            @Param("estado") EstadoServicio estado,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax
    );

    // Búsqueda por ciudad
    @Query("SELECT s FROM Servicio s JOIN s.ubicacion u WHERE " +
            "u.ciudad = :ciudad AND s.estado = :estado")
    List<Servicio> buscarPorCiudad(
            @Param("ciudad") String ciudad,
            @Param("estado") EstadoServicio estado
    );

    // Búsqueda avanzada con múltiples filtros
    @Query("SELECT s FROM Servicio s JOIN s.ubicacion u WHERE " +
            "(:deporte IS NULL OR s.deporte = :deporte) AND " +
            "(:ciudad IS NULL OR u.ciudad = :ciudad) AND " +
            "(:precioMin IS NULL OR s.precio >= :precioMin) AND " +
            "(:precioMax IS NULL OR s.precio <= :precioMax) AND " +
            "s.estado = 'PUBLICADO'")
    Page<Servicio> buscarConFiltros(
            @Param("deporte") String deporte,
            @Param("ciudad") String ciudad,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            Pageable pageable
    );

    // Servicios mejor calificados
    @Query("SELECT s FROM Servicio s WHERE s.estado = 'PUBLICADO' " +
            "AND s.calificacionPromedio >= :calificacionMinima " +
            "ORDER BY s.calificacionPromedio DESC, s.totalResenas DESC")
    List<Servicio> findTopServiciosMejorCalificados(
            @Param("calificacionMinima") BigDecimal calificacionMinima,
            Pageable pageable
    );

    // Búsqueda por texto (nombre o descripción)
    @Query("SELECT s FROM Servicio s WHERE " +
            "(LOWER(s.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(s.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) AND " +
            "s.estado = 'PUBLICADO'")
    Page<Servicio> buscarPorTexto(@Param("texto") String texto, Pageable pageable);

    // Estadísticas
    @Query("SELECT COUNT(s) FROM Servicio s WHERE s.estado = 'PUBLICADO'")
    Long contarServiciosPublicados();

    @Query("SELECT s.deporte, COUNT(s) FROM Servicio s WHERE s.estado = 'PUBLICADO' GROUP BY s.deporte")
    List<Object[]> contarServiciosPorDeporte();
}
