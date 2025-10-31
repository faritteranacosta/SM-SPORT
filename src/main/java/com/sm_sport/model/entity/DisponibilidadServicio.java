package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;


@Entity
@Table(name = "disponibilidad_servicio", indexes = {
        @Index(name = "idx_disponibilidad_servicio", columnList = "id_servicio"),
        @Index(name = "idx_disponibilidad_fecha", columnList = "fecha"),
        @Index(name = "idx_disponibilidad_disponible", columnList = "disponible")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_disponibilidad", length = 50)
    private String idDisponibilidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    @JsonIgnoreProperties({"disponibilidad", "resenas", "reservas"})
    private Servicio servicio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private Boolean disponible = true;

    @Column(name = "cupos_disponibles")
    private Integer cuposDisponibles = 1;
}
