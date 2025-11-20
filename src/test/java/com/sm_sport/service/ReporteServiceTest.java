package com.sm_sport.service;

import com.sm_sport.dto.response.EstadisticasResponse;
import com.sm_sport.dto.response.ReporteDesempenoResponse;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.ReporteMapper;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.*;
import com.sm_sport.repository.*;
import com.sm_sport.service.impl.ReporteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReporteService - Tests Unitarios")
class ReporteServiceTest {

    // Dependencias inyectadas en ReporteServiceImpl (MOCKS)
    @Mock
    private ReporteDesempenoRepository reporteRepository;
    @Mock
    private ProveedorRepository proveedorRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private ResenaRepository resenaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ServicioRepository servicioRepository;
    @Mock
    private DenunciaRepository denunciaRepository;
    @Mock
    private ReporteMapper reporteMapper;

    // Clase a probar (INJECT MOCKS)
    @InjectMocks
    private ReporteServiceImpl reporteService;

    // Mocks de entidades
    private Proveedor mockProveedor;
    private Servicio mockServicio;
    private Reserva mockReservaFinalizada;
    private Reserva mockReservaCancelada;
    private Resena mockResenaBuena;
    private ReporteDesempeno mockReporteGuardado;

    private final String PROV_ID = "P001";
    private final LocalDate FECHA_INICIO = LocalDate.of(2025, 1, 1);
    private final LocalDate FECHA_FIN = LocalDate.of(2025, 1, 31);

    @BeforeEach
    void setUp() {
        // 1. Inicialización de Proveedor
        mockProveedor = new Proveedor();
        mockProveedor.setIdUsuario(PROV_ID);
        mockProveedor.setNombre("Proveedor Test");
        mockProveedor.setEstado(EstadoUsuario.ACTIVO);

        // 2. Inicialización de Servicio
        mockServicio = new Servicio();
        mockServicio.setIdServicio("S001");
        mockServicio.setDeporte("Fútbol");
        mockServicio.setEstado(EstadoServicio.PUBLICADO);

        // Usando UbicacionServicio.builder()
        mockServicio.setUbicacion(
                UbicacionServicio.builder()
                        .ciudad("Bogotá")
                        .direccion("Calle 1 # 1-1")
                        .departamento("Cundinamarca")
                        .pais("Colombia")
                        .build()
        );

        // Relacionar Servicio con Proveedor
        mockProveedor.setServiciosPublicados(List.of(mockServicio));

        // 3. Inicialización de Reservas
        mockReservaFinalizada = new Reserva();
        mockReservaFinalizada.setEstado(EstadoReserva.FINALIZADA);
        mockReservaFinalizada.setCostoTotal(BigDecimal.valueOf(100.00));
        mockReservaFinalizada.setProveedor(mockProveedor);

        mockReservaCancelada = new Reserva();
        mockReservaCancelada.setEstado(EstadoReserva.CANCELADA);
        mockReservaCancelada.setCostoTotal(BigDecimal.ZERO);
        mockReservaCancelada.setProveedor(mockProveedor);

        // 4. Inicialización de Reseñas
        mockResenaBuena = new Resena();
        mockResenaBuena.setCalificacion(5);
        mockResenaBuena.setEstadoRevision(EstadoRevision.PUBLICADA);
        mockResenaBuena.setServicio(mockServicio);
        mockResenaBuena.setFechaCreacion(LocalDateTime.of(2025, 1, 15, 10, 0));

        // 5. Inicialización de Reporte Guardado
        mockReporteGuardado = ReporteDesempeno.builder()
                .idReporte("REP-001")
                .totalVentas(1)
                .ingresosGenerados(BigDecimal.valueOf(100.00))
                .build();
    }

    // =============================================================
    // generarReporteProveedor()
    // =============================================================

    @Test
    @DisplayName("generarReporteProveedor - Exitoso con datos válidos")
    void testGenerarReporteProveedor_Exitoso() {
        // Arrange
        List<Reserva> reservas = List.of(mockReservaFinalizada, mockReservaCancelada);
        List<Resena> resenas = List.of(mockResenaBuena);

        when(proveedorRepository.findById(PROV_ID)).thenReturn(Optional.of(mockProveedor));
        when(reservaRepository.findReservasEnRango(FECHA_INICIO, FECHA_FIN)).thenReturn(reservas);
        when(resenaRepository.findByServicioIdServicio(mockServicio.getIdServicio())).thenReturn(resenas);
        when(reporteRepository.save(any(ReporteDesempeno.class))).thenReturn(mockReporteGuardado);

        ReporteDesempenoResponse responseMock = ReporteDesempenoResponse.builder()
                .idReporte("REP-001")
                .totalVentas(1)
                .reservasCanceladas(1)
                .ingresosGenerados(BigDecimal.valueOf(100.00))
                .calificacionPromedio(BigDecimal.valueOf(5.00))
                .build();
        when(reporteMapper.toReporteResponse(mockReporteGuardado)).thenReturn(responseMock);

        // Act
        ReporteDesempenoResponse result = reporteService.generarReporteProveedor(
                PROV_ID, FECHA_INICIO, FECHA_FIN);

        // Assert
        assertThat(result).isNotNull();

        verify(proveedorRepository).findById(PROV_ID);
        verify(reservaRepository).findReservasEnRango(FECHA_INICIO, FECHA_FIN);
        verify(reporteRepository).save(any(ReporteDesempeno.class));
    }

    @Test
    @DisplayName("generarReporteProveedor - Proveedor no encontrado lanza ResourceNotFoundException")
    void testGenerarReporteProveedor_ProveedorNoEncontrado() {
        // Arrange
        when(proveedorRepository.findById(PROV_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reporteService.generarReporteProveedor(
                PROV_ID, FECHA_INICIO, FECHA_FIN))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proveedor no encontrado");
    }

    @Test
    @DisplayName("generarReporteProveedor - Fechas invertidas lanza IllegalArgumentException")
    void testGenerarReporteProveedor_FechasInvalidas() {
        // Arrange
        LocalDate fechaInicio = LocalDate.of(2025, 2, 1);
        LocalDate fechaFin = LocalDate.of(2025, 1, 1);
        when(proveedorRepository.findById(PROV_ID)).thenReturn(Optional.of(mockProveedor));

        // Act & Assert
        assertThatThrownBy(() -> reporteService.generarReporteProveedor(
                PROV_ID, fechaInicio, fechaFin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de fin no puede ser anterior");
    }

    // =============================================================
    // obtenerEstadisticasGenerales()
    // =============================================================

    @Test
    @DisplayName("obtenerEstadisticasGenerales - Retorna todas las métricas correctamente")
    void testObtenerEstadisticasGenerales_Exitoso() {
        // Arrange: Configuración de todas las llamadas a Repositorios

        // 1. Usuarios
        when(usuarioRepository.count()).thenReturn(100L);
        when(usuarioRepository.countByActivo(true)).thenReturn(90L);
        when(usuarioRepository.countByRol(Rol.CLIENTE)).thenReturn(80L);
        when(usuarioRepository.countByRol(Rol.PROVEEDOR)).thenReturn(20L);
        when(usuarioRepository.countByRol(Rol.ADMINISTRADOR)).thenReturn(5L);

        // 2. Servicios
        when(servicioRepository.count()).thenReturn(50L);
        when(servicioRepository.countByEstado(EstadoServicio.PUBLICADO)).thenReturn(40L);

        Servicio s1 = crearServicioMock("S1", "Fútbol", "Bogotá");
        Servicio s2 = crearServicioMock("S2", "Fútbol", "Bogotá");
        Servicio s3 = crearServicioMock("S3", "Baloncesto", "Medellín");
        when(servicioRepository.findAll()).thenReturn(List.of(s1, s2, s3));

        // 3. Reservas
        when(reservaRepository.count()).thenReturn(200L);

        List<Reserva> confirmadas = List.of(mock(Reserva.class), mock(Reserva.class)); // 2
        List<Reserva> canceladas = List.of(mock(Reserva.class)); // 1
        List<Reserva> finalizadas = List.of(mockReservaFinalizada, mockReservaFinalizada); // 2

        when(reservaRepository.findByEstado(EstadoReserva.CONFIRMADA)).thenReturn(confirmadas);
        when(reservaRepository.findByEstado(EstadoReserva.CANCELADA)).thenReturn(canceladas);
        when(reservaRepository.findByEstado(EstadoReserva.FINALIZADA)).thenReturn(finalizadas);

        // 4. Reseñas / Calidad
        when(resenaRepository.count()).thenReturn(500L);

        Resena r1 = crearResenaMock(4, EstadoRevision.PUBLICADA);
        Resena r2 = crearResenaMock(5, EstadoRevision.PUBLICADA);
        when(resenaRepository.findByEstadoRevision(EstadoRevision.PUBLICADA)).thenReturn(List.of(r1, r2));

        // 5. Denuncias
        when(denunciaRepository.findByEstado(EstadoDenuncia.PENDIENTE)).thenReturn(List.of(mock(Denuncia.class))); // 1
        when(denunciaRepository.findByEstado(EstadoDenuncia.ATENDIDA)).thenReturn(List.of(mock(Denuncia.class), mock(Denuncia.class))); // 2

        // Act
        EstadisticasResponse result = reporteService.obtenerEstadisticasGenerales();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIngresosGenerados()).isEqualTo(BigDecimal.valueOf(200.00));
        assertThat(result.getCalificacionPromedio()).isEqualTo(BigDecimal.valueOf(4.50).setScale(2));
    }

    // =============================================================
    // listarReportes()
    // =============================================================

    @Test
    @DisplayName("listarReportes - Retorna la lista de reportes del proveedor")
    void testListarReportes_Exitoso() {
        // Arrange
        ReporteDesempeno reporte1 = mock(ReporteDesempeno.class);
        ReporteDesempeno reporte2 = mock(ReporteDesempeno.class);
        List<ReporteDesempeno> reportes = List.of(reporte1, reporte2);

        when(proveedorRepository.existsById(PROV_ID)).thenReturn(true);
        when(reporteRepository.findReportesByProveedorOrdenados(PROV_ID)).thenReturn(reportes);

        ReporteDesempenoResponse resp1 = ReporteDesempenoResponse.builder().idReporte("R1").build();
        ReporteDesempenoResponse resp2 = ReporteDesempenoResponse.builder().idReporte("R2").build();
        when(reporteMapper.toReporteResponseList(reportes)).thenReturn(List.of(resp1, resp2));

        // Act
        List<ReporteDesempenoResponse> result = reporteService.listarReportes(PROV_ID);

        // Assert
        assertThat(result).hasSize(2);
        verify(proveedorRepository).existsById(PROV_ID);
        verify(reporteRepository).findReportesByProveedorOrdenados(PROV_ID);
    }

    @Test
    @DisplayName("listarReportes - Proveedor no existe lanza ResourceNotFoundException")
    void testListarReportes_ProveedorNoExiste() {
        // Arrange
        when(proveedorRepository.existsById(PROV_ID)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> reporteService.listarReportes(PROV_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proveedor no encontrado con ID: " + PROV_ID);
    }

    // =============================================================
    // generarReportesMensuales()
    // =============================================================

    @Test
    @DisplayName("generarReportesMensuales - Genera reportes solo para proveedores activos y con servicios, y maneja errores")
    void testGenerarReportesMensuales_LogicaCorrecta() throws Exception {
        // Usamos spy para interceptar la llamada al método interno generarReporteProveedor()
        ReporteServiceImpl reporteServiceSpy = spy(reporteService);

        // Arrange
        YearMonth mesAnterior = YearMonth.now().minusMonths(1);
        LocalDate expectedFechaInicio = mesAnterior.atDay(1);
        LocalDate expectedFechaFin = mesAnterior.atEndOfMonth();

        // 1. Proveedor Elegible (Activo y > 0 servicios) -> DEBE ser llamado
        Proveedor p1 = new Proveedor();
        p1.setIdUsuario("P001");
        p1.setEstado(EstadoUsuario.ACTIVO);
        p1.setTotalServiciosPublicados(5);

        // 2. Proveedor No Elegible (Activo pero sin servicios) -> NO debe ser llamado
        Proveedor p2 = new Proveedor();
        p2.setIdUsuario("P002");
        p2.setEstado(EstadoUsuario.ACTIVO);
        p2.setTotalServiciosPublicados(0);

        // 3. Proveedor No Elegible (Inactivo) -> NO debe ser llamado
        Proveedor p3 = new Proveedor();
        p3.setIdUsuario("P003");
        p3.setEstado(EstadoUsuario.INACTIVO);
        p3.setTotalServiciosPublicados(10);

        // 4. Proveedor Elegible que causa excepción (para probar el try-catch interno) -> DEBE ser llamado
        Proveedor p4 = new Proveedor();
        p4.setIdUsuario("P004");
        p4.setEstado(EstadoUsuario.ACTIVO);
        p4.setTotalServiciosPublicados(2);

        List<Proveedor> todosProveedores = List.of(p1, p2, p3, p4);

        when(proveedorRepository.findAll()).thenReturn(todosProveedores);

        // **CORRECCIÓN** Usamos doReturn(null) en lugar de doNothing() porque generarReporteProveedor devuelve ReporteDesempenoResponse.
        doReturn(null).when(reporteServiceSpy).generarReporteProveedor(
                eq("P001"),
                eq(expectedFechaInicio),
                eq(expectedFechaFin)
        );

        // Stub el método interno en el Spy para simular un error en P4 (esto sí es válido con doThrow)
        doThrow(new ResourceNotFoundException("Error simulado de Proveedor")).when(reporteServiceSpy).generarReporteProveedor(eq("P004"), any(), any());

        // Act
        reporteServiceSpy.generarReportesMensuales();

        // Assert & Verify
        // 1. Verificar que se obtuvo la lista completa
        verify(proveedorRepository).findAll();

        // 2. Verificar que generarReporteProveedor fue llamado 2 veces (P001 y P004)
        verify(reporteServiceSpy, times(2)).generarReporteProveedor(any(), any(), any());

        // 3. Verificar las llamadas con los parámetros correctos para P001 (Éxito)
        verify(reporteServiceSpy, times(1)).generarReporteProveedor(
                eq("P001"),
                eq(expectedFechaInicio),
                eq(expectedFechaFin)
        );

        // 4. Verificar las llamadas para P004 (Error manejado)
        verify(reporteServiceSpy, times(1)).generarReporteProveedor(
                eq("P004"),
                eq(expectedFechaInicio),
                eq(expectedFechaFin)
        );

        // 5. Verificar que NO fue llamado para los proveedores no elegibles (P002 y P003)
        verify(reporteServiceSpy, never()).generarReporteProveedor(eq("P002"), any(), any());
        verify(reporteServiceSpy, never()).generarReporteProveedor(eq("P003"), any(), any());
    }

    // =============================================================
    // Métodos Helper para Mocks (Para Estadísticas)
    // =============================================================

    private Servicio crearServicioMock(String id, String deporte, String ciudad) {
        Servicio s = new Servicio();
        s.setIdServicio(id);
        s.setDeporte(deporte);
        s.setEstado(EstadoServicio.PUBLICADO);

        // Usando UbicacionServicio y setear campos obligatorios.
        UbicacionServicio u = UbicacionServicio.builder()
                .ciudad(ciudad)
                .direccion("Direccion Test")
                .departamento("Departamento Test")
                .pais("Pais Test")
                .build();

        s.setUbicacion(u);
        return s;
    }

    private Resena crearResenaMock(int calificacion, EstadoRevision estado) {
        Resena r = new Resena();
        r.setCalificacion(calificacion);
        r.setEstadoRevision(estado);
        return r;
    }
}