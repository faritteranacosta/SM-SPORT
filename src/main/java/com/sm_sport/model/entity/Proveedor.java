package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "proveedores")
@PrimaryKeyJoinColumn(name = "id_proveedor")
@DiscriminatorValue("PROVEEDOR")
@Getter
@Setter
@NoArgsConstructor
public class Proveedor extends Usuario {

    @Column(name = "saldo_cuenta", precision = 10, scale = 2)
    private BigDecimal saldoCuenta = BigDecimal.ZERO;

    @Column(name = "calificacion_promedio", precision = 3, scale = 2)
    private BigDecimal calificacionPromedio = BigDecimal.ZERO;

    @Column(name = "total_servicios_publicados")
    private Integer totalServiciosPublicados = 0;

    @Column(name = "total_reservas_completadas")
    private Integer totalReservasCompletadas = 0;

    @Column(name = "descripcion_negocio", columnDefinition = "TEXT")
    private String descripcionNegocio;

    @Column(nullable = false)
    private Boolean verificado = false;

    // Relación con Servicios (agregación)
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("proveedor")
    private List<Servicio> serviciosPublicados = new ArrayList<>();

    // Relación con Reservas
    @OneToMany(mappedBy = "proveedor")
    @JsonIgnoreProperties("proveedor")
    private List<Reserva> reservas = new ArrayList<>();

    // Relación con Reportes de Desempeño
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("proveedor")
    private List<ReporteDesempeno> reportes = new ArrayList<>();
}
