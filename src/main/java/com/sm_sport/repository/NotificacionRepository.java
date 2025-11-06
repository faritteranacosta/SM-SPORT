package com.sm_sport.repository;

import com.sm_sport.model.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, String> {

    // Notificaciones por usuario
    List<Notificacion> findByUsuarioIdUsuario(String idUsuario);

    Page<Notificacion> findByUsuarioIdUsuario(String idUsuario, Pageable pageable);

    // Notificaciones no leídas por usuario
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.idUsuario = :idUsuario " +
            "AND n.leida = false ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findNotificacionesNoLeidas(@Param("idUsuario") String idUsuario);

    // Contar notificaciones no leídas
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.usuario.idUsuario = :idUsuario " +
            "AND n.leida = false")
    Long contarNotificacionesNoLeidas(@Param("idUsuario") String idUsuario);

    // Notificaciones por tipo
    List<Notificacion> findByTipoNotificacion(String tipoNotificacion);

    // Notificaciones por usuario y tipo
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.idUsuario = :idUsuario " +
            "AND n.tipoNotificacion = :tipo ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findNotificacionesByUsuarioAndTipo(
            @Param("idUsuario") String idUsuario,
            @Param("tipo") String tipo
    );

    // Marcar notificación como leída
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLectura = :fechaLectura " +
            "WHERE n.idNotificacion = :idNotificacion")
    void marcarComoLeida(
            @Param("idNotificacion") String idNotificacion,
            @Param("fechaLectura") LocalDateTime fechaLectura
    );

    // Marcar todas como leídas para un usuario
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLectura = :fechaLectura " +
            "WHERE n.usuario.idUsuario = :idUsuario AND n.leida = false")
    void marcarTodasComoLeidas(
            @Param("idUsuario") String idUsuario,
            @Param("fechaLectura") LocalDateTime fechaLectura
    );

    // Eliminar notificaciones antiguas
    @Modifying
    @Query("DELETE FROM Notificacion n WHERE n.fechaEnvio < :fechaLimite AND n.leida = true")
    void eliminarNotificacionesAntiguas(@Param("fechaLimite") LocalDateTime fechaLimite);
}
