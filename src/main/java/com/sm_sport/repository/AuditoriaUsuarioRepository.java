package com.sm_sport.repository;

import com.sm_sport.model.entity.AuditoriaUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaUsuarioRepository extends JpaRepository<AuditoriaUsuario, String> {

    // Auditoría por usuario
    List<AuditoriaUsuario> findByUsuarioIdUsuario(String idUsuario);

    Page<AuditoriaUsuario> findByUsuarioIdUsuario(String idUsuario, Pageable pageable);

    // Auditoría por acción
    List<AuditoriaUsuario> findByAccion(String accion);

    // Auditoría por usuario y acción
    @Query("SELECT a FROM AuditoriaUsuario a WHERE a.usuario.idUsuario = :idUsuario " +
            "AND a.accion = :accion ORDER BY a.fechaAccion DESC")
    List<AuditoriaUsuario> findAuditoriaByUsuarioAndAccion(
            @Param("idUsuario") String idUsuario,
            @Param("accion") String accion
    );

    // Auditoría en rango de fechas
    @Query("SELECT a FROM AuditoriaUsuario a WHERE a.fechaAccion BETWEEN :inicio AND :fin " +
            "ORDER BY a.fechaAccion DESC")
    List<AuditoriaUsuario> findAuditoriaEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Últimas acciones de un usuario
    @Query("SELECT a FROM AuditoriaUsuario a WHERE a.usuario.idUsuario = :idUsuario " +
            "ORDER BY a.fechaAccion DESC")
    List<AuditoriaUsuario> findUltimasAcciones(@Param("idUsuario") String idUsuario, Pageable pageable);

    // Estadísticas de acciones
    @Query("SELECT a.accion, COUNT(a) FROM AuditoriaUsuario a GROUP BY a.accion")
    List<Object[]> contarAccionesPorTipo();
}
