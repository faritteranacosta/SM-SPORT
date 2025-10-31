package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sm_sport.model.enums.EstadoServicio;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "servicios", indexes = {
        @Index(name = "idx_servicio_proveedor", columnList = "id_proveedor"),
        @Index(name = "idx_servicio_deporte", columnList = "deporte"),
        @Index(name = "idx_servicio_estado", columnList = "estado"),
        @Index(name = "idx_servicio_precio", columnList = "precio")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_servicio", length = 50)
    private String idServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    @JsonIgnoreProperties({"serviciosPublicados", "reservas", "reportes"})
    private Proveedor proveedor;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String deporte;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoServicio estado = EstadoServicio.PUBLICADO;

    @CreatedDate
    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "calificacion_promedio", precision = 3, scale = 2)
    private BigDecimal calificacionPromedio = BigDecimal.ZERO;

    @Column(name = "total_resenas")
    private Integer totalResenas = 0;

    // Composición con Ubicacion (1:1)
    @OneToOne(mappedBy = "servicio", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("servicio")
    private UbicacionServicio ubicacion;

    // Composición con Disponibilidad (1:N)
    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("servicio")
    private List<DisponibilidadServicio> disponibilidad = new ArrayList<>();

    // Agregación con Reseñas
    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("servicio")
    private List<Resena> resenas = new ArrayList<>();

    // Relación con Reservas
    @OneToMany(mappedBy = "servicio")
    @JsonIgnoreProperties("servicio")
    private List<Reserva> reservas = new ArrayList<>();
}
