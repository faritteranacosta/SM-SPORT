package com.sm_sport.repository;

import com.sm_sport.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {

    // Búsqueda por preferencias
    @Query("SELECT c FROM Cliente c WHERE c.preferenciaDeportes LIKE %:deporte%")
    List<Cliente> findByPreferenciaDeporte(@Param("deporte") String deporte);

    // Clientes por nivel de experiencia
    List<Cliente> findByNivelExperiencia(String nivelExperiencia);

    // Clientes por rango de edad
    @Query("SELECT c FROM Cliente c WHERE c.fechaNacimiento BETWEEN :fechaInicio AND :fechaFin")
    List<Cliente> findByRangoEdad(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    // Estadísticas
    @Query("SELECT COUNT(c) FROM Cliente c JOIN c.historialReservas r WHERE r.estado = 'FINALIZADA'")
    Long contarClientesConReservas();
}
