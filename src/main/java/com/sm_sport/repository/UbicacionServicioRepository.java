package com.sm_sport.repository;

import com.sm_sport.model.entity.UbicacionServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UbicacionServicioRepository extends JpaRepository<UbicacionServicio, String> {

    // Búsqueda por servicio
    Optional<UbicacionServicio> findByServicioIdServicio(String idServicio);

    // Búsqueda por ubicación
    List<UbicacionServicio> findByCiudad(String ciudad);

    List<UbicacionServicio> findByDepartamento(String departamento);

    List<UbicacionServicio> findByCiudadAndDepartamento(String ciudad, String departamento);

    // Búsqueda por proximidad (radio en kilómetros)
    @Query(value = "SELECT * FROM ubicaciones_servicio u WHERE " +
            "6371 * ACOS(COS(RADIANS(:latitud)) * COS(RADIANS(u.coordenadas_lat)) * " +
            "COS(RADIANS(u.coordenadas_lng) - RADIANS(:longitud)) + " +
            "SIN(RADIANS(:latitud)) * SIN(RADIANS(u.coordenadas_lat))) <= :radioKm",
            nativeQuery = true)
    List<UbicacionServicio> findServiciosCercanos(
            @Param("latitud") BigDecimal latitud,
            @Param("longitud") BigDecimal longitud,
            @Param("radioKm") Integer radioKm
    );

    // Estadísticas
    @Query("SELECT u.ciudad, COUNT(u) FROM UbicacionServicio u GROUP BY u.ciudad ORDER BY COUNT(u) DESC")
    List<Object[]> contarServiciosPorCiudad();
}
