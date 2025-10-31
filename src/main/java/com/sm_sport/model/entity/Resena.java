package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sm_sport.model.enums.EstadoRevision;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "resenas", indexes = {
        @Index(name = "idx_resena_servicio", columnList = "id_servicio"),
        @Index(name = "idx_resena_cliente", columnList = "id_cliente"),
        @Index(name = "idx_resena_calificacion", columnList = "calificacion"),
        @Index(name = "idx_resena_reportada", columnList = "reportada")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_resena", length = 50)
    private String idResena;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    @JsonIgnoreProperties({"disponibilidad", "resenas", "reservas"})
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @JsonIgnoreProperties({"historialReservas", "resenas", "pagos", "solicitudesReembolso"})
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    @JsonIgnoreProperties({"pago", "resena", "solicitudReembolso", "auditoria"})
    private Reserva reserva;

    @Column(nullable = false)
    private Integer calificacion; // 1-5

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "respuesta_proveedor", columnDefinition = "TEXT")
    private String respuestaProveedor;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(nullable = false)
    private Boolean reportada = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_revision", length = 20)
    private EstadoRevision estadoRevision = EstadoRevision.PUBLICADA;
}
