package com.sm_sport.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sm_sport.model.enums.EstadoPago;
import com.sm_sport.model.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "pagos", indexes = {
        @Index(name = "idx_pago_cliente", columnList = "id_cliente"),
        @Index(name = "idx_pago_estado", columnList = "estado_pago"),
        @Index(name = "idx_pago_referencia", columnList = "referencia_pago")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pago", length = 50)
    private String idPago;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    @JsonIgnoreProperties({"pago", "resena", "solicitudReembolso", "auditoria"})
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @JsonIgnoreProperties({"historialReservas", "resenas", "pagos", "solicitudesReembolso"})
    private Cliente cliente;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "referencia_pago", unique = true, length = 100)
    private String referenciaPago;

    @CreatedDate
    @Column(name = "fecha_pago", nullable = false, updatable = false)
    private LocalDateTime fechaPago;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "proveedor_pasarela", length = 50)
    private String proveedorPasarela;

    @Column(name = "token_transaccion", length = 255)
    private String tokenTransaccion;

    // Relación 1:1 con Comprobante
    @OneToOne(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("pago")
    private Comprobante comprobante;
}
