package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_usuarios", indexes = {
        @Index(name = "idx_auditoria_usuario", columnList = "id_usuario"),
        @Index(name = "idx_auditoria_fecha", columnList = "fecha_accion"),
        @Index(name = "idx_auditoria_accion", columnList = "accion")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_auditoria", length = 50)
    private String idAuditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({"historialReservas", "resenas", "pagos", "serviciosPublicados"})
    private Usuario usuario;

    @Column(nullable = false, length = 50)
    private String accion; // LOGIN, LOGOUT, EDITAR_PERFIL, CAMBIO_ESTADO

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @CreatedDate
    @Column(name = "fecha_accion", nullable = false, updatable = false)
    private LocalDateTime fechaAccion;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;
}
