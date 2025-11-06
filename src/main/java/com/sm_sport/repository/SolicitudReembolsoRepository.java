package com.sm_sport.repository;

import com.sm_sport.model.entity.SolicitudReembolso;
import com.sm_sport.model.enums.EstadoReembolso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudReembolsoRepository extends JpaRepository<SolicitudReembolso, String> {

    // Solicitud por reserva
    Optional<SolicitudReembolso> findByReservaIdReserva(String idReserva);

    // Solicitudes por cliente
    List<SolicitudReembolso> findByClienteIdUsuario(String idCliente);

    // Solicitudes por estado
    List<SolicitudReembolso> findByEstado(EstadoReembolso estado);

    // Solicitudes pendientes
    @Query("SELECT s FROM SolicitudReembolso s WHERE s.estado IN ('SOLICITADO', 'EN_REVISION') " +
            "ORDER BY s.fechaSolicitud ASC")
    List<SolicitudReembolso> findSolicitudesPendientes();

    // Solicitudes en rango de fechas
    @Query("SELECT s FROM SolicitudReembolso s WHERE s.fechaSolicitud BETWEEN :inicio AND :fin")
    List<SolicitudReembolso> findSolicitudesEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Estadísticas
    @Query("SELECT COUNT(s) FROM SolicitudReembolso s WHERE s.estado = 'APROBADO'")
    Long contarReembolsosAprobados();
}
