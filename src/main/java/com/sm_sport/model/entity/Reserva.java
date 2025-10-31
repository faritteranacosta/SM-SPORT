package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sm_sport.model.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservas", indexes = {
        @Index(name = "idx_reserva_cliente", columnList = "id_cliente"),
        @Index(name = "idx_reserva_servicio", columnList = "id_servicio"),
        @Index(name = "idx_reserva_proveedor", columnList = "id_proveedor"),
        @Index(name = "idx_reserva_estado", columnList = "estado"),
        @Index(name = "idx_reserva_fecha", columnList = "fecha_reserva")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_reserva", length = 50)
    private String idReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @JsonIgnoreProperties({"historialReservas", "resenas", "pagos", "solicitudesReembolso"})
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    @JsonIgnoreProperties({"disponibilidad", "resenas", "reservas"})
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    @JsonIgnoreProperties({"serviciosPublicados", "reservas", "reportes"})
    private Proveedor proveedor;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDate fechaReserva;

    @Column(name = "hora_reserva", nullable = false)
    private LocalTime horaReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "costo_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "notas_cliente", columnDefinition = "TEXT")
    private String notasCliente;

    // Relación 1:1 con Pago
    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("reserva")
    private Pago pago;

    // Relación 1:1 con Reseña
    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("reserva")
    private Resena resena;

    // Relación 1:1 con Solicitud de Reembolso
    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("reserva")
    private SolicitudReembolso solicitudReembolso;

    // Relación con Auditoría
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("reserva")
    private List<AuditoriaReserva> auditoria = new ArrayList<>();
}
