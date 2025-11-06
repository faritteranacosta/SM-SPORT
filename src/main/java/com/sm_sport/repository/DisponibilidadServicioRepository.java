package com.sm_sport.repository;

import com.sm_sport.model.entity.DisponibilidadServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DisponibilidadServicioRepository extends JpaRepository<DisponibilidadServicio, String> {

    // Disponibilidad por servicio
    List<DisponibilidadServicio> findByServicioIdServicio(String idServicio);

    // Disponibilidad por servicio y fecha
    @Query("SELECT d FROM DisponibilidadServicio d WHERE " +
            "d.servicio.idServicio = :idServicio AND d.fecha = :fecha " +
            "ORDER BY d.horaInicio")
    List<DisponibilidadServicio> findByServicioAndFecha(
            @Param("idServicio") String idServicio,
            @Param("fecha") LocalDate fecha
    );

    // Disponibilidad por rango de fechas
    @Query("SELECT d FROM DisponibilidadServicio d WHERE " +
            "d.servicio.idServicio = :idServicio " +
            "AND d.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "AND d.disponible = true " +
            "ORDER BY d.fecha, d.horaInicio")
    List<DisponibilidadServicio> findDisponibilidadEnRango(
            @Param("idServicio") String idServicio,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    // Verificar disponibilidad específica
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DisponibilidadServicio d WHERE " +
            "d.servicio.idServicio = :idServicio AND d.fecha = :fecha " +
            "AND :hora BETWEEN d.horaInicio AND d.horaFin " +
            "AND d.disponible = true AND d.cuposDisponibles > 0")
    Boolean verificarDisponibilidad(
            @Param("idServicio") String idServicio,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora
    );

    // Eliminar disponibilidades antiguas
    @Query("DELETE FROM DisponibilidadServicio d WHERE d.fecha < :fecha")
    void eliminarDisponibilidadesAntiguas(@Param("fecha") LocalDate fecha);
}
