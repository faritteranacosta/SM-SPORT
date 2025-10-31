package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_desempeno", indexes = {
        @Index(name = "idx_reporte_proveedor", columnList = "id_proveedor"),
        @Index(name = "idx_reporte_fecha", columnList = "fecha_generacion")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteDesempeno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_reporte", length = 50)
    private String idReporte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    @JsonIgnoreProperties({"serviciosPublicados", "reservas", "reportes"})
    private Proveedor proveedor;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @CreatedDate
    @Column(name = "fecha_generacion", nullable = false, updatable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "total_ventas")
    private Integer totalVentas = 0;

    @Column(name = "reservas_canceladas")
    private Integer reservasCanceladas = 0;

    @Column(name = "ingresos_generados", precision = 12, scale = 2)
    private BigDecimal ingresosGenerados = BigDecimal.ZERO;

    @Column(name = "calificacion_promedio", precision = 3, scale = 2)
    private BigDecimal calificacionPromedio = BigDecimal.ZERO;

    @Column(name = "total_resenas")
    private Integer totalResenas = 0;
}
