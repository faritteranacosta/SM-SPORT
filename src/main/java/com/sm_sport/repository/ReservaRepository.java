package com.sm_sport.repository;

import com.sm_sport.model.entity.Reserva;
import com.sm_sport.model.enums.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, String> {

    // Reservas por cliente
    List<Reserva> findByClienteIdUsuario(String idCliente);

    Page<Reserva> findByClienteIdUsuario(String idCliente, Pageable pageable);

    @Query("SELECT r FROM Reserva r WHERE r.cliente.idUsuario = :idCliente " +
            "ORDER BY r.fechaReserva DESC, r.horaReserva DESC")
    List<Reserva> findReservasByClienteOrdenadas(@Param("idCliente") String idCliente);

    // Reservas por proveedor
    List<Reserva> findByProveedorIdUsuario(String idProveedor);

    Page<Reserva> findByProveedorIdUsuario(String idProveedor, Pageable pageable);

    // Reservas por servicio
    List<Reserva> findByServicioIdServicio(String idServicio);

    // Reservas por estado
    List<Reserva> findByEstado(EstadoReserva estado);

    Page<Reserva> findByEstado(EstadoReserva estado, Pageable pageable);

    // Reservas por cliente y estados
    @Query("SELECT r FROM Reserva r WHERE r.cliente.idUsuario = :idCliente " +
            "AND r.estado IN :estados ORDER BY r.fechaReserva DESC")
    List<Reserva> findReservasByClienteAndEstados(
            @Param("idCliente") String idCliente,
            @Param("estados") List<EstadoReserva> estados
    );

    // Reservas por proveedor y estado
    @Query("SELECT r FROM Reserva r WHERE r.proveedor.idUsuario = :idProveedor " +
            "AND r.estado = :estado ORDER BY r.fechaCreacion DESC")
    List<Reserva> findReservasByProveedorAndEstado(
            @Param("idProveedor") String idProveedor,
            @Param("estado") EstadoReserva estado
    );

    // Reservas próximas
    @Query("SELECT r FROM Reserva r WHERE r.cliente.idUsuario = :idCliente " +
            "AND r.estado IN ('PENDIENTE', 'CONFIRMADA') " +
            "AND r.fechaReserva >= :fechaActual " +
            "ORDER BY r.fechaReserva ASC, r.horaReserva ASC")
    List<Reserva> findReservasProximas(
            @Param("idCliente") String idCliente,
            @Param("fechaActual") LocalDate fechaActual
    );

    // Reservas por fecha
    @Query("SELECT r FROM Reserva r WHERE r.fechaReserva BETWEEN :fechaInicio AND :fechaFin")
    List<Reserva> findReservasEnRango(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    // Verificar conflicto de reserva
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reserva r WHERE " +
            "r.servicio.idServicio = :idServicio " +
            "AND r.fechaReserva = :fecha " +
            "AND r.horaReserva = :hora " +
            "AND r.estado IN ('PENDIENTE', 'CONFIRMADA')")
    Boolean existeReservaEnHorario(
            @Param("idServicio") String idServicio,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora
    );

    // Reservas pendientes antiguas (para cancelar automáticamente)
    @Query("SELECT r FROM Reserva r WHERE r.estado = 'PENDIENTE' " +
            "AND r.fechaCreacion < :fechaLimite")
    List<Reserva> findReservasPendientesAntiguas(@Param("fechaLimite") LocalDateTime fechaLimite);

    // Estadísticas
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.estado = 'FINALIZADA' " +
            "AND r.fechaReserva BETWEEN :inicio AND :fin")
    Long contarReservasFinalizadasEnPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );

    @Query("SELECT SUM(r.costoTotal) FROM Reserva r WHERE r.estado = 'FINALIZADA' " +
            "AND r.fechaReserva BETWEEN :inicio AND :fin")
    BigDecimal calcularIngresosEnPeriodo(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );
}
