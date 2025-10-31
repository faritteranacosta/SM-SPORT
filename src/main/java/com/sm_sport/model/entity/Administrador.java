package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "administradores")
@PrimaryKeyJoinColumn(name = "id_administrador")
@DiscriminatorValue("ADMINISTRADOR")
@Getter
@Setter
@NoArgsConstructor
public class Administrador extends Usuario {

    @Column(name = "nivel_acceso", length = 20)
    private String nivelAcceso; // SUPER_ADMIN, ADMIN, MODERADOR

    @Column(name = "permisos_especiales", columnDefinition = "TEXT")
    private String permisosEspeciales; // JSON o texto con permisos adicionales

    // Relación con Denuncias atendidas
    @OneToMany(mappedBy = "administrador")
    @JsonIgnoreProperties("administrador")
    private List<Denuncia> denunciasAtendidas = new ArrayList<>();
}
