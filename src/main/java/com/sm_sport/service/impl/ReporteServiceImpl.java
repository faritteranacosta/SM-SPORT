package com.sm_sport.service.impl;

import com.sm_sport.dto.response.EstadisticasResponse;
import com.sm_sport.dto.response.ReporteDesempenoResponse;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.ReporteMapper;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.*;
import com.sm_sport.repository.*;
import com.sm_sport.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteServiceImpl implements ReporteService {

    private final ReporteDesempenoRepository reporteRepository;
    private final ProveedorRepository proveedorRepository;
    private final ReservaRepository reservaRepository;
    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final DenunciaRepository denunciaRepository;
    private final ReporteMapper reporteMapper;

    @Override
    @Transactional
    public ReporteDesempenoResponse generarReporteProveedor(String idProveedor,
                                                            LocalDate fechaInicio,
                                                            LocalDate fechaFin) {
        log.info("Generando reporte de desempeño para proveedor: {} desde {} hasta {}",
                idProveedor, fechaInicio, fechaFin);

        // Validar que el proveedor existe
        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proveedor no encontrado con ID: " + idProveedor));

        // Validar fechas
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        // Obtener todas las reservas del proveedor en el periodo
        List<Reserva> reservasEnPeriodo = reservaRepository.findReservasEnRango(fechaInicio, fechaFin)
                .stream()
                .filter(r -> r.getProveedor().getIdUsuario().equals(idProveedor))
                .collect(Collectors.toList());

        // Calcular métricas
        Integer totalVentas = calcularTotalVentas(reservasEnPeriodo);
        Integer reservasCanceladas = calcularReservasCanceladas(reservasEnPeriodo);
        BigDecimal ingresosGenerados = calcularIngresosGenerados(reservasEnPeriodo);

        // Obtener reseñas del proveedor en el periodo
        List<Resena> resenasProveedor = obtenerResenasProveedorEnPeriodo(proveedor, fechaInicio, fechaFin);
        BigDecimal calificacionPromedio = calcularCalificacionPromedio(resenasProveedor);
        Integer totalResenas = resenasProveedor.size();

        // Crear entidad de reporte
        ReporteDesempeno reporte = ReporteDesempeno.builder()
                .proveedor(proveedor)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalVentas(totalVentas)
                .reservasCanceladas(reservasCanceladas)
                .ingresosGenerados(ingresosGenerados)
                .calificacionPromedio(calificacionPromedio)
                .totalResenas(totalResenas)
                .build();

        // Guardar reporte
        ReporteDesempeno reporteGuardado = reporteRepository.save(reporte);

        log.info("Reporte generado exitosamente con ID: {}", reporteGuardado.getIdReporte());

        return reporteMapper.toReporteResponse(reporteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteDesempenoResponse> listarReportes(String idProveedor) {
        log.info("Listando reportes del proveedor: {}", idProveedor);

        // Validar que el proveedor existe
        if (!proveedorRepository.existsById(idProveedor)) {
            throw new ResourceNotFoundException("Proveedor no encontrado con ID: " + idProveedor);
        }

        List<ReporteDesempeno> reportes = reporteRepository.findReportesByProveedorOrdenados(idProveedor);

        log.info("Se encontraron {} reportes para el proveedor {}", reportes.size(), idProveedor);

        return reporteMapper.toReporteResponseList(reportes);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasResponse obtenerEstadisticasGenerales() {
        log.info("Obteniendo estadísticas generales del sistema");

        EstadisticasResponse estadisticas = EstadisticasResponse.builder()
                // Estadísticas de usuarios
                .totalUsuarios(usuarioRepository.count())
                .usuariosActivos(usuarioRepository.countByActivo(true))
                .totalClientes(usuarioRepository.countByRol(Rol.CLIENTE))
                .totalProveedores(usuarioRepository.countByRol(Rol.PROVEEDOR))
                .usuariosPorRol(calcularUsuariosPorRol())

                // Estadísticas de servicios
                .totalServicios(servicioRepository.count())
                .serviciosPublicados(servicioRepository.countByEstado(EstadoServicio.PUBLICADO))
                .serviciosPorDeporte(calcularServiciosPorDeporte())
                .serviciosPorCiudad(calcularServiciosPorCiudad())

                // Estadísticas de reservas
                .totalReservas(reservaRepository.count())
                .reservasConfirmadas((long) reservaRepository.findByEstado(EstadoReserva.CONFIRMADA).size())
                .reservasCanceladas((long) reservaRepository.findByEstado(EstadoReserva.CANCELADA).size())
                .reservasFinalizadas((long) reservaRepository.findByEstado(EstadoReserva.FINALIZADA).size())
                .ingresosGenerados(calcularIngresosTotal())

                // Estadísticas de calidad
                .calificacionPromedio(calcularCalificacionPromedioGeneral())
                .totalResenas(resenaRepository.count())

                // Estadísticas de denuncias
                .denunciasPendientes((long) denunciaRepository.findByEstado(EstadoDenuncia.PENDIENTE).size())
                .denunciasAtendidas((long) denunciaRepository.findByEstado(EstadoDenuncia.ATENDIDA).size())
                .build();

        log.info("Estadísticas generales calculadas exitosamente");

        return estadisticas;
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 1 * *") // Se ejecuta el día 1 de cada mes a las 2:00 AM
    public void generarReportesMensuales() {
        log.info("Iniciando generación automática de reportes mensuales");

        // Obtener mes anterior
        YearMonth mesAnterior = YearMonth.now().minusMonths(1);
        LocalDate fechaInicio = mesAnterior.atDay(1);
        LocalDate fechaFin = mesAnterior.atEndOfMonth();

        log.info("Generando reportes del periodo: {} al {}", fechaInicio, fechaFin);

        // Obtener todos los proveedores activos
        // Obtener todos los proveedores activos
        List<Proveedor> proveedoresActivos = proveedorRepository.findAll()
                .stream()
                .filter(p -> {
                    EstadoUsuario estado = p.getEstado();
                    return estado != null && estado == EstadoUsuario.ACTIVO && p.getTotalServiciosPublicados() > 0;
                })
                .collect(Collectors.toList());

        log.info("Se encontraron {} proveedores activos", proveedoresActivos.size());

        // Generar reporte para cada proveedor
        int reportesGenerados = 0;
        int reportesConError = 0;

        for (Proveedor proveedor : proveedoresActivos) {
            try {
                generarReporteProveedor(proveedor.getIdUsuario(), fechaInicio, fechaFin);
                reportesGenerados++;
                log.debug("Reporte generado para proveedor: {}", proveedor.getNombre());
            } catch (Exception e) {
                reportesConError++;
                log.error("Error al generar reporte para proveedor {}: {}",
                        proveedor.getIdUsuario(), e.getMessage());
            }
        }

        log.info("Generación de reportes mensuales completada. " +
                "Exitosos: {}, Con errores: {}", reportesGenerados, reportesConError);
    }

    // ==================== MÉTODOS PRIVADOS DE CÁLCULO ====================

    private Integer calcularTotalVentas(List<Reserva> reservas) {
        return (int) reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.FINALIZADA)
                .count();
    }

    private Integer calcularReservasCanceladas(List<Reserva> reservas) {
        return (int) reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CANCELADA)
                .count();
    }

    private BigDecimal calcularIngresosGenerados(List<Reserva> reservas) {
        return reservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.FINALIZADA)
                .map(Reserva::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Resena> obtenerResenasProveedorEnPeriodo(Proveedor proveedor,
                                                          LocalDate inicio,
                                                          LocalDate fin) {
        // Obtener todos los servicios del proveedor
        return proveedor.getServiciosPublicados().stream()
                .flatMap(servicio -> resenaRepository.findByServicioIdServicio(servicio.getIdServicio()).stream())
                .filter(resena -> resena.getEstadoRevision() == EstadoRevision.PUBLICADA)
                .filter(resena -> {
                    LocalDate fechaResena = resena.getFechaCreacion().toLocalDate();
                    return !fechaResena.isBefore(inicio) && !fechaResena.isAfter(fin);
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calcularCalificacionPromedio(List<Resena> resenas) {
        if (resenas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double promedio = resenas.stream()
                .mapToInt(Resena::getCalificacion)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Long> calcularUsuariosPorRol() {
        Map<String, Long> usuariosPorRol = new HashMap<>();
        usuariosPorRol.put("CLIENTE", usuarioRepository.countByRol(Rol.CLIENTE));
        usuariosPorRol.put("PROVEEDOR", usuarioRepository.countByRol(Rol.PROVEEDOR));
        usuariosPorRol.put("ADMINISTRADOR", usuarioRepository.countByRol(Rol.ADMINISTRADOR));
        return usuariosPorRol;
    }

    private Map<String, Long> calcularServiciosPorDeporte() {
        return servicioRepository.findAll().stream()
                .filter(s -> s.getEstado() == EstadoServicio.PUBLICADO)
                .collect(Collectors.groupingBy(
                        Servicio::getDeporte,
                        Collectors.counting()
                ));
    }

    private Map<String, Long> calcularServiciosPorCiudad() {
        return servicioRepository.findAll().stream()
                .filter(s -> s.getEstado() == EstadoServicio.PUBLICADO && s.getUbicacion() != null)
                .collect(Collectors.groupingBy(
                        servicio -> servicio.getUbicacion().getCiudad(),
                        Collectors.counting()
                ));
    }

    private BigDecimal calcularIngresosTotal() {
        List<Reserva> reservasFinalizadas = reservaRepository.findByEstado(EstadoReserva.FINALIZADA);
        return reservasFinalizadas.stream()
                .map(Reserva::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularCalificacionPromedioGeneral() {
        List<Resena> todasResenas = resenaRepository.findByEstadoRevision(EstadoRevision.PUBLICADA);

        if (todasResenas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double promedio = todasResenas.stream()
                .mapToInt(Resena::getCalificacion)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }
}