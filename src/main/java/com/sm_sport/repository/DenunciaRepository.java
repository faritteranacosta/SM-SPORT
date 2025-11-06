package com.sm_sport.repository;

import com.sm_sport.model.entity.Denuncia;
import com.sm_sport.model.enums.EstadoDenuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, String> {

    // Denuncias por usuario denunciante
    List<Denuncia> findByUsuarioDenuncianteIdUsuario(String idUsuario);

    // Denuncias contra un usuario
    List<Denuncia> findByUsuarioDenunciadoIdUsuario(String idUsuario);

    // Denuncias por estado
    List<Denuncia> findByEstado(EstadoDenuncia estado);

    // Denuncias pendientes
    @Query("SELECT d FROM Denuncia d WHERE d.estado = 'PENDIENTE' ORDER BY d.fechaDenuncia ASC")
    List<Denuncia> findDenunciasPendientes();

    // Denuncias por tipo
    List<Denuncia> findByTipoDenuncia(String tipoDenuncia);

    // Denuncias atendidas por administrador
    List<Denuncia> findByAdministradorIdUsuario(String idAdministrador);

    // Denuncias en rango de fechas
    @Query("SELECT d FROM Denuncia d WHERE d.fechaDenuncia BETWEEN :inicio AND :fin")
    List<Denuncia> findDenunciasEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Contar denuncias contra un usuario
    @Query("SELECT COUNT(d) FROM Denuncia d WHERE d.usuarioDenunciado.idUsuario = :idUsuario " +
            "AND d.estado = 'ATENDIDA'")
    Long contarDenunciasAtendidasContraUsuario(@Param("idUsuario") String idUsuario);

    // Estadísticas
    @Query("SELECT d.tipoDenuncia, COUNT(d) FROM Denuncia d GROUP BY d.tipoDenuncia")
    List<Object[]> contarDenunciasPorTipo();
}
