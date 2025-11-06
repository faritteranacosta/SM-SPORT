package com.sm_sport.repository;

import com.sm_sport.model.entity.Resena;
import com.sm_sport.model.enums.EstadoRevision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, String> {

    // Reseña por reserva
    Optional<Resena> findByReservaIdReserva(String idReserva);

    // Reseñas por servicio
    List<Resena> findByServicioIdServicio(String idServicio);

    Page<Resena> findByServicioIdServicio(String idServicio, Pageable pageable);

    // Reseñas publicadas por servicio
    @Query("SELECT r FROM Resena r WHERE r.servicio.idServicio = :idServicio " +
            "AND r.estadoRevision = 'PUBLICADA' ORDER BY r.fechaCreacion DESC")
    List<Resena> findResenasByServicio(@Param("idServicio") String idServicio);

    // Reseñas por cliente
    List<Resena> findByClienteIdUsuario(String idCliente);

    // Reseñas por calificación
    List<Resena> findByCalificacion(Integer calificacion);

    // Reseñas reportadas
    @Query("SELECT r FROM Resena r WHERE r.reportada = true AND r.estadoRevision = 'EN_REVISION'")
    List<Resena> findResenasReportadas();

    // Reseñas por estado de revisión
    List<Resena> findByEstadoRevision(EstadoRevision estadoRevision);

    // Calcular promedio de calificaciones por servicio
    @Query("SELECT AVG(r.calificacion) FROM Resena r WHERE r.servicio.idServicio = :idServicio " +
            "AND r.estadoRevision = 'PUBLICADA'")
    BigDecimal calcularPromedioCalificacion(@Param("idServicio") String idServicio);

    // Estadísticas
    @Query("SELECT COUNT(r) FROM Resena r WHERE r.servicio.idServicio = :idServicio " +
            "AND r.estadoRevision = 'PUBLICADA'")
    Long contarResenasPorServicio(@Param("idServicio") String idServicio);
}
