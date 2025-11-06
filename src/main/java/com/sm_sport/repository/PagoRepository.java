package com.sm_sport.repository;

import com.sm_sport.model.entity.Pago;
import com.sm_sport.model.enums.EstadoPago;
import com.sm_sport.model.enums.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, String> {

    // Pago por reserva
    Optional<Pago> findByReservaIdReserva(String idReserva);

    // Pagos por cliente
    List<Pago> findByClienteIdUsuario(String idCliente);

    // Pagos por estado
    List<Pago> findByEstadoPago(EstadoPago estadoPago);

    // Pagos por método
    List<Pago> findByMetodoPago(MetodoPago metodoPago);

    // Pago por referencia
    Optional<Pago> findByReferenciaPago(String referenciaPago);

    // Pagos pendientes por cliente
    @Query("SELECT p FROM Pago p WHERE p.cliente.idUsuario = :idCliente " +
            "AND p.estadoPago = 'PENDIENTE'")
    List<Pago> findPagosPendientesByCliente(@Param("idCliente") String idCliente);

    // Pagos en rango de fechas
    @Query("SELECT p FROM Pago p WHERE p.fechaPago BETWEEN :inicio AND :fin")
    List<Pago> findPagosEnRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Estadísticas
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.estadoPago = 'APROBADO' " +
            "AND p.fechaPago BETWEEN :inicio AND :fin")
    BigDecimal calcularTotalPagosAprobados(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("SELECT p.metodoPago, COUNT(p) FROM Pago p WHERE p.estadoPago = 'APROBADO' " +
            "GROUP BY p.metodoPago")
    List<Object[]> contarPagosPorMetodo();
}
