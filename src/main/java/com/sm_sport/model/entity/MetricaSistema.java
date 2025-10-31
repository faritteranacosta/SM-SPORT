package com.sm_sport.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "metricas_sistema", indexes = {
        @Index(name = "idx_metrica_nombre", columnList = "nombre_metrica"),
        @Index(name = "idx_metrica_fecha", columnList = "fecha_medicion"),
        @Index(name = "idx_metrica_categoria", columnList = "categoria")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_metrica", length = 50)
    private String idMetrica;

    @Column(name = "nombre_metrica", nullable = false, length = 100)
    private String nombreMetrica;

    @Column(name = "valor_metrica", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMetrica;

    @Column(length = 20)
    private String unidad;

    @CreatedDate
    @Column(name = "fecha_medicion", nullable = false, updatable = false)
    private LocalDateTime fechaMedicion;

    @Column(length = 20)
    private String periodo; // DIARIO, SEMANAL, MENSUAL, ANUAL

    @Column(length = 50)
    private String categoria; // USUARIOS, SERVICIOS, RESERVAS, INGRESOS
}
