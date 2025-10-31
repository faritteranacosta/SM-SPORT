package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "idx_notificacion_usuario", columnList = "id_usuario"),
        @Index(name = "idx_notificacion_leida", columnList = "leida"),
        @Index(name = "idx_notificacion_tipo", columnList = "tipo_notificacion"),
        @Index(name = "idx_notificacion_fecha", columnList = "fecha_envio")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_notificacion", length = 50)
    private String idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"historialReservas", "resenas", "pagos", "serviciosPublicados"})
    private Usuario usuario;

    @Column(name = "tipo_notificacion", nullable = false, length = 50)
    private String tipoNotificacion; // RESERVA, PAGO, CANCELACION, RESENA, SISTEMA

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    private Boolean leida = false;

    @CreatedDate
    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;
}
