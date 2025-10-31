package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ubicaciones_servicio", indexes = {
        @Index(name = "idx_ubicacion_ciudad", columnList = "ciudad"),
        @Index(name = "idx_ubicacion_departamento", columnList = "departamento")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_ubicacion", length = 50)
    private String idUbicacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false, unique = true)
    @JsonIgnoreProperties({"ubicacion", "disponibilidad", "resenas", "reservas"})
    private Servicio servicio;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(nullable = false, length = 100)
    private String departamento;

    @Column(nullable = false, length = 100)
    private String pais;

    @Column(name = "coordenadas_lat", precision = 10, scale = 8)
    private BigDecimal coordenadasLat;

    @Column(name = "coordenadas_lng", precision = 11, scale = 8)
    private BigDecimal coordenadasLng;
}
