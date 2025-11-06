package com.sm_sport.repository;

import com.sm_sport.model.entity.Usuario;
import com.sm_sport.model.enums.EstadoUsuario;
import com.sm_sport.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String>,
        JpaSpecificationExecutor<Usuario> {

    // Búsqueda básica
    Optional<Usuario> findByCorreo(String correo);

    Boolean existsByCorreo(String correo);

    List<Usuario> findByEstado(EstadoUsuario estado);

    List<Usuario> findByRol(Rol rol);

    // Búsquedas avanzadas
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Usuario> buscarPorNombre(@Param("nombre") String nombre);

    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.estado = :estado")
    List<Usuario> findByRolAndEstado(@Param("rol") Rol rol, @Param("estado") EstadoUsuario estado);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado = 'ACTIVO'")
    Long contarUsuariosActivos();

    @Query("SELECT u FROM Usuario u WHERE u.fechaRegistro BETWEEN :inicio AND :fin")
    List<Usuario> findUsuariosRegistradosEntre(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Estadísticas
    @Query("SELECT u.rol, COUNT(u) FROM Usuario u GROUP BY u.rol")
    List<Object[]> contarUsuariosPorRol();
}
