package com.sm_sport.repository;

import com.sm_sport.model.entity.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, String> {

    // Administradores por nivel de acceso
    List<Administrador> findByNivelAcceso(String nivelAcceso);

    // Verificar si existe super admin
    boolean existsByNivelAcceso(String nivelAcceso);
}
