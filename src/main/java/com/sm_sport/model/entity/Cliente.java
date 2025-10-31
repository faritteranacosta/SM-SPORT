package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "id_cliente")
@DiscriminatorValue("CLIENTE")
@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Usuario {

    @Column(name = "preferencias_deportes", columnDefinition = "TEXT")
    private String preferenciaDeportes;

    @Column(name = "nivel_experiencia", length = 50)
    private String nivelExperiencia;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    // Relación con Reservas (agregación)
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"cliente", "servicio", "proveedor"})
    private List<Reserva> historialReservas = new ArrayList<>();

    // Relación con Reseñas
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("cliente")
    private List<Resena> resenas = new ArrayList<>();

    // Relación con Pagos
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("cliente")
    private List<Pago> pagos = new ArrayList<>();

    // Relación con Solicitudes de Reembolso
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("cliente")
    private List<SolicitudReembolso> solicitudesReembolso = new ArrayList<>();
}
