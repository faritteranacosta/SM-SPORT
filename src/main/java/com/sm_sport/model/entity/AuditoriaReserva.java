package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_reservas", indexes = {
        @Index(name = "idx_auditoria_reserva", columnList = "id_reserva"),
        @Index(name = "idx_auditoria_reserva_fecha", columnList = "fecha_cambio")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_auditoria", length = 50)
    private String idAuditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false)
    @JsonIgnoreProperties({"pago", "resena", "solicitudReembolso", "auditoria"})
    private Reserva reserva;

    @Column(name = "estado_anterior", length = 20)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private String estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_accion")
    @JsonIgnoreProperties({"historialReservas", "resenas", "pagos", "serviciosPublicados"})
    private Usuario usuarioAccion;

    @CreatedDate
    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    private LocalDateTime fechaCambio;

    @Column(columnDefinition = "TEXT")
    private String motivo;
}
