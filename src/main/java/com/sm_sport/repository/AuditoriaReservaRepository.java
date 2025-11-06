package com.sm_sport.repository;

import com.sm_sport.model.entity.AuditoriaReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaReservaRepository extends JpaRepository<AuditoriaReserva, String> {

    // Auditoría por reserva
    List<AuditoriaReserva> findByReservaIdReserva(String idReserva);

    // Auditoría por usuario que realizó la acción
    List<AuditoriaReserva> findByUsuarioAccionIdUsuario(String idUsuario);

    // Auditoría en rango de fechas
    @Query("SELECT a FROM AuditoriaReserva a WHERE a.fechaCambio BETWEEN :inicio AND :fin " +
            "ORDER BY a.fechaCambio DESC")
    List<AuditoriaReserva> findAuditoriaEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Cambios de estado específicos
    @Query("SELECT a FROM AuditoriaReserva a WHERE a.estadoNuevo = :estado " +
            "ORDER BY a.fechaCambio DESC")
    List<AuditoriaReserva> findCambiosAEstado(@Param("estado") String estado);

    // Historial completo de una reserva
    @Query("SELECT a FROM AuditoriaReserva a WHERE a.reserva.idReserva = :idReserva " +
            "ORDER BY a.fechaCambio ASC")
    List<AuditoriaReserva> findHistorialReserva(@Param("idReserva") String idReserva);
}
