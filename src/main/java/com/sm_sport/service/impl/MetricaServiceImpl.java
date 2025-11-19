package com.sm_sport.service.impl;

import com.sm_sport.dto.response.KPIResponse;
import com.sm_sport.dto.response.MetricaResponse;
import com.sm_sport.model.entity.KPI;
import com.sm_sport.model.entity.MetricaSistema;
import com.sm_sport.model.entity.Reserva;
import com.sm_sport.model.enums.EstadoServicio;
import com.sm_sport.repository.*;
import com.sm_sport.service.MetricaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricaServiceImpl implements MetricaService {

    private final MetricaSistemaRepository metricaRepository;
    private final KPIRepository kpiRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final ServicioRepository servicioRepository;

    @Override
    @Transactional
    public void registrarMetrica(String nombre, Double valor, String unidad, String categoria) {
        log.info("Registrando métrica: {} = {} {}", nombre, valor, unidad);

        MetricaSistema metrica = MetricaSistema.builder()
                .nombreMetrica(nombre)
                .valorMetrica(BigDecimal.valueOf(valor))
                .unidad(unidad)
                .categoria(categoria)
                .periodo("DIARIO")
                .fechaMedicion(LocalDateTime.now())
                .build();

        metricaRepository.save(metrica);

        log.info("Métrica registrada exitosamente: {}", nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetricaResponse> obtenerPorCategoria(String categoria) {
        log.info("Obteniendo métricas por categoría: {}", categoria);

        List<MetricaSistema> metricas = metricaRepository.findByCategoria(categoria);

        return metricas.stream()
                .map(this::toMetricaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KPIResponse> calcularKPIs() {
        log.info("Calculando KPIs del sistema");

        List<KPIResponse> kpis = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();
        String periodo = "MENSUAL";

        // KPI 1: Total de usuarios activos
        kpis.add(calcularKPIUsuariosActivos(ahora, periodo));

        // KPI 2: Tasa de conversión (reservas confirmadas vs total)
        kpis.add(calcularKPITasaConversion(ahora, periodo));

        // KPI 3: Ingreso promedio por reserva
        kpis.add(calcularKPIIngresoPromedio(ahora, periodo));

        // KPI 4: Servicios publicados
        kpis.add(calcularKPIServiciosPublicados(ahora, periodo));

        // KPI 5: Tasa de ocupación
        kpis.add(calcularKPITasaOcupacion(ahora, periodo));

        log.info("Se calcularon {} KPIs exitosamente", kpis.size());

        return kpis;
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * *") // Ejecuta diariamente a la 1:00 AM
    public void generarMetricasDiarias() {
        log.info("Iniciando generación automática de métricas diarias");

        try {
            LocalDate hoy = LocalDate.now();
            LocalDateTime inicioDia = hoy.atStartOfDay();
            LocalDateTime finDia = hoy.atTime(LocalTime.MAX);

            // Métrica 1: Total de usuarios registrados
            Long totalUsuarios = usuarioRepository.count();
            registrarMetrica("Total Usuarios", totalUsuarios.doubleValue(), "usuarios", "USUARIOS");

            // Métrica 2: Usuarios activos (con al menos una reserva)
            Long usuariosActivos = usuarioRepository.contarUsuariosActivos();
            registrarMetrica("Usuarios Activos", usuariosActivos.doubleValue(), "usuarios", "USUARIOS");

            // Métrica 3: Total de reservas del día
            // ✅ CORRECTO
            List<Reserva> reservasHoy = reservaRepository.findReservasEnRango(hoy, hoy);
            registrarMetrica("Reservas del Día", (double) reservasHoy.size(), "reservas", "RESERVAS");

            // Métrica 4: Ingresos del día
            BigDecimal ingresosHoy = pagoRepository.calcularTotalPagosAprobados(inicioDia, finDia);
            if (ingresosHoy == null) ingresosHoy = BigDecimal.ZERO;
            registrarMetrica("Ingresos del Día", ingresosHoy.doubleValue(), "COP", "INGRESOS");

            // Métrica 5: Servicios publicados
            Long serviciosPublicados = servicioRepository.contarServiciosPublicados();
            registrarMetrica("Servicios Publicados", serviciosPublicados.doubleValue(), "servicios", "SERVICIOS");

            // Métrica 6: Reservas confirmadas del mes
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            Long reservasMes = reservaRepository.contarReservasFinalizadasEnPeriodo(inicioMes, hoy);
            registrarMetrica("Reservas Mes Actual", reservasMes.doubleValue(), "reservas", "RESERVAS");

            // Métrica 7: Ingresos del mes
            LocalDateTime inicioMesDateTime = inicioMes.atStartOfDay();
            BigDecimal ingresosMes = reservaRepository.calcularIngresosEnPeriodo(inicioMes, hoy);
            if (ingresosMes == null) ingresosMes = BigDecimal.ZERO;
            registrarMetrica("Ingresos Mes Actual", ingresosMes.doubleValue(), "COP", "INGRESOS");

            // Generar y guardar KPIs
            guardarKPIsCalculados();

            log.info("Métricas diarias generadas exitosamente");

        } catch (Exception e) {
            log.error("Error al generar métricas diarias: {}", e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS PRIVADOS DE CÁLCULO DE KPIs ====================

    private KPIResponse calcularKPIUsuariosActivos(LocalDateTime fecha, String periodo) {
        Long usuariosActivos = usuarioRepository.contarUsuariosActivos();
        Long totalUsuarios = usuarioRepository.count();

        BigDecimal valorKpi = BigDecimal.valueOf(usuariosActivos);
        BigDecimal metaObjetivo = BigDecimal.valueOf(totalUsuarios * 0.7); // Meta: 70% de usuarios activos
        BigDecimal porcentajeCumplimiento = calcularPorcentajeCumplimiento(valorKpi, metaObjetivo);

        // Determinar tendencia comparando con KPI anterior
        String tendencia = determinarTendencia("Usuarios Activos", valorKpi);

        KPI kpi = KPI.builder()
                .nombreKpi("Usuarios Activos")
                .valorKpi(valorKpi)
                .periodo(periodo)
                .tendencia(tendencia)
                .fechaCalculo(fecha)
                .metaObjetivo(metaObjetivo)
                .porcentajeCumplimiento(porcentajeCumplimiento)
                .build();

        kpiRepository.save(kpi);

        return toKPIResponse(kpi);
    }

    private KPIResponse calcularKPITasaConversion(LocalDateTime fecha, String periodo) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        Long totalReservas = reservaRepository.count();
        Long reservasFinalizadas = reservaRepository.contarReservasFinalizadasEnPeriodo(inicioMes, hoy);

        BigDecimal tasaConversion = BigDecimal.ZERO;
        if (totalReservas > 0) {
            tasaConversion = BigDecimal.valueOf(reservasFinalizadas)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalReservas), 2, RoundingMode.HALF_UP);
        }

        BigDecimal metaObjetivo = BigDecimal.valueOf(80); // Meta: 80% de conversión
        BigDecimal porcentajeCumplimiento = calcularPorcentajeCumplimiento(tasaConversion, metaObjetivo);
        String tendencia = determinarTendencia("Tasa de Conversión", tasaConversion);

        KPI kpi = KPI.builder()
                .nombreKpi("Tasa de Conversión")
                .valorKpi(tasaConversion)
                .periodo(periodo)
                .tendencia(tendencia)
                .fechaCalculo(fecha)
                .metaObjetivo(metaObjetivo)
                .porcentajeCumplimiento(porcentajeCumplimiento)
                .build();

        kpiRepository.save(kpi);

        return toKPIResponse(kpi);
    }

    private KPIResponse calcularKPIIngresoPromedio(LocalDateTime fecha, String periodo) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        BigDecimal ingresosTotal = reservaRepository.calcularIngresosEnPeriodo(inicioMes, hoy);
        Long totalReservas = reservaRepository.contarReservasFinalizadasEnPeriodo(inicioMes, hoy);

        BigDecimal ingresoPromedio = BigDecimal.ZERO;
        if (totalReservas > 0 && ingresosTotal != null) {
            ingresoPromedio = ingresosTotal.divide(
                    BigDecimal.valueOf(totalReservas),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal metaObjetivo = BigDecimal.valueOf(50000); // Meta: $50,000 COP promedio
        BigDecimal porcentajeCumplimiento = calcularPorcentajeCumplimiento(ingresoPromedio, metaObjetivo);
        String tendencia = determinarTendencia("Ingreso Promedio", ingresoPromedio);

        KPI kpi = KPI.builder()
                .nombreKpi("Ingreso Promedio por Reserva")
                .valorKpi(ingresoPromedio)
                .periodo(periodo)
                .tendencia(tendencia)
                .fechaCalculo(fecha)
                .metaObjetivo(metaObjetivo)
                .porcentajeCumplimiento(porcentajeCumplimiento)
                .build();

        kpiRepository.save(kpi);

        return toKPIResponse(kpi);
    }

    private KPIResponse calcularKPIServiciosPublicados(LocalDateTime fecha, String periodo) {
        Long serviciosPublicados = servicioRepository.contarServiciosPublicados();

        BigDecimal valorKpi = BigDecimal.valueOf(serviciosPublicados);
        BigDecimal metaObjetivo = BigDecimal.valueOf(100); // Meta: 100 servicios publicados
        BigDecimal porcentajeCumplimiento = calcularPorcentajeCumplimiento(valorKpi, metaObjetivo);
        String tendencia = determinarTendencia("Servicios Publicados", valorKpi);

        KPI kpi = KPI.builder()
                .nombreKpi("Servicios Publicados")
                .valorKpi(valorKpi)
                .periodo(periodo)
                .tendencia(tendencia)
                .fechaCalculo(fecha)
                .metaObjetivo(metaObjetivo)
                .porcentajeCumplimiento(porcentajeCumplimiento)
                .build();

        kpiRepository.save(kpi);

        return toKPIResponse(kpi);
    }

    private KPIResponse calcularKPITasaOcupacion(LocalDateTime fecha, String periodo) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        Long totalReservas = reservaRepository.contarReservasFinalizadasEnPeriodo(inicioMes, hoy);
        Long serviciosDisponibles = servicioRepository.countByEstado(EstadoServicio.PUBLICADO);

        BigDecimal tasaOcupacion = BigDecimal.ZERO;
        if (serviciosDisponibles > 0) {
            // Asumiendo que cada servicio puede tener máximo 30 reservas al mes
            Long capacidadTotal = serviciosDisponibles * 30;
            tasaOcupacion = BigDecimal.valueOf(totalReservas)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(capacidadTotal), 2, RoundingMode.HALF_UP);
        }

        BigDecimal metaObjetivo = BigDecimal.valueOf(60); // Meta: 60% de ocupación
        BigDecimal porcentajeCumplimiento = calcularPorcentajeCumplimiento(tasaOcupacion, metaObjetivo);
        String tendencia = determinarTendencia("Tasa de Ocupación", tasaOcupacion);

        KPI kpi = KPI.builder()
                .nombreKpi("Tasa de Ocupación")
                .valorKpi(tasaOcupacion)
                .periodo(periodo)
                .tendencia(tendencia)
                .fechaCalculo(fecha)
                .metaObjetivo(metaObjetivo)
                .porcentajeCumplimiento(porcentajeCumplimiento)
                .build();

        kpiRepository.save(kpi);

        return toKPIResponse(kpi);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void guardarKPIsCalculados() {
        LocalDateTime ahora = LocalDateTime.now();
        String periodo = "DIARIO";

        calcularKPIUsuariosActivos(ahora, periodo);
        calcularKPITasaConversion(ahora, periodo);
        calcularKPIIngresoPromedio(ahora, periodo);
        calcularKPIServiciosPublicados(ahora, periodo);
        calcularKPITasaOcupacion(ahora, periodo);
    }

    private BigDecimal calcularPorcentajeCumplimiento(BigDecimal valor, BigDecimal meta) {
        if (meta.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return valor.multiply(BigDecimal.valueOf(100))
                .divide(meta, 2, RoundingMode.HALF_UP);
    }

    private String determinarTendencia(String nombreKpi, BigDecimal valorActual) {
        Optional<KPI> kpiAnterior = kpiRepository.findUltimoKPI(nombreKpi);

        if (kpiAnterior.isEmpty()) {
            return "ESTABLE";
        }

        BigDecimal valorAnterior = kpiAnterior.get().getValorKpi();
        int comparacion = valorActual.compareTo(valorAnterior);

        if (comparacion > 0) {
            return "ASCENDENTE";
        } else if (comparacion < 0) {
            return "DESCENDENTE";
        } else {
            return "ESTABLE";
        }
    }

    // ==================== MAPPERS ====================

    private MetricaResponse toMetricaResponse(MetricaSistema metrica) {
        return MetricaResponse.builder()
                .idMetrica(metrica.getIdMetrica())
                .nombreMetrica(metrica.getNombreMetrica())
                .valorMetrica(metrica.getValorMetrica())
                .unidad(metrica.getUnidad())
                .fechaMedicion(metrica.getFechaMedicion())
                .periodo(metrica.getPeriodo())
                .categoria(metrica.getCategoria())
                .build();
    }

    private KPIResponse toKPIResponse(KPI kpi) {
        return KPIResponse.builder()
                .idKpi(kpi.getIdKpi())
                .nombreKpi(kpi.getNombreKpi())
                .valorKpi(kpi.getValorKpi())
                .periodo(kpi.getPeriodo())
                .tendencia(kpi.getTendencia())
                .fechaCalculo(kpi.getFechaCalculo())
                .metaObjetivo(kpi.getMetaObjetivo())
                .porcentajeCumplimiento(kpi.getPorcentajeCumplimiento())
                .build();
    }
}