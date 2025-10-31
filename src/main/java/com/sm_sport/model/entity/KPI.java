package com.sm_sport.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "kpis", indexes = {
        @Index(name = "idx_kpi_nombre", columnList = "nombre_kpi"),
        @Index(name = "idx_kpi_fecha", columnList = "fecha_calculo")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KPI {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_kpi", length = 50)
    private String idKpi;

    @Column(name = "nombre_kpi", nullable = false, length = 100)
    private String nombreKpi;

    @Column(name = "valor_kpi", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorKpi;

    @Column(nullable = false, length = 20)
    private String periodo;

    @Column(length = 20)
    private String tendencia; // ASCENDENTE, DESCENDENTE, ESTABLE

    @CreatedDate
    @Column(name = "fecha_calculo", nullable = false, updatable = false)
    private LocalDateTime fechaCalculo;

    @Column(name = "meta_objetivo", precision = 15, scale = 2)
    private BigDecimal metaObjetivo;

    @Column(name = "porcentaje_cumplimiento", precision = 5, scale = 2)
    private BigDecimal porcentajeCumplimiento;
}
