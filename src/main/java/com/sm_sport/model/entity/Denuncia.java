package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sm_sport.model.enums.EstadoDenuncia;
import com.sm_sport.model.enums.AccionDenuncia;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Table(name = "denuncias", indexes = {
        @Index(name = "idx_denuncia_denunciante", columnList = "id_usuario_denunciante"),
        @Index(name = "idx_denuncia_denunciado", columnList = "id_usuario_denunciado"),
        @Index(name = "idx_denuncia_estado", columnList = "estado"),
        @Index(name = "idx_denuncia_tipo", columnList = "tipo_denuncia")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_denuncia", length = 50)
    private String idDenuncia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_denunciante", nullable = false)
    @JsonIgnoreProperties({"historialReservas", "resenas", "pagos", "serviciosPublicados"})
    private Usuario usuarioDenunciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_denunciado", nullable = false)
    @JsonIgnoreProperties({"historialReservas", "resenas", "pagos", "serviciosPublicados"})
    private Usuario usuarioDenunciado;

    @Column(name = "tipo_denuncia", nullable = false, length = 50)
    private String tipoDenuncia; // SERVICIO_FRAUDULENTO, COMPORTAMIENTO_INAPROPIADO, INCUMPLIMIENTO, OTRO

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String evidencia;

    @CreatedDate
    @Column(name = "fecha_denuncia", nullable = false, updatable = false)
    private LocalDateTime fechaDenuncia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDenuncia estado = EstadoDenuncia.PENDIENTE;

    @Column(name = "respuesta_admin", columnDefinition = "TEXT")
    private String respuestaAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion_tomada", length = 50)
    private AccionDenuncia accionTomada;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador")
    @JsonIgnoreProperties({"denunciasAtendidas"})
    private Administrador administrador;
}
