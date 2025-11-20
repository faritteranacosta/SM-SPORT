package com.sm_sport.service;

import com.sm_sport.dto.request.ReembolsoRequest;
import com.sm_sport.dto.response.SolicitudReembolsoResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.ReembolsoMapper;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.EstadoPago;
import com.sm_sport.model.enums.EstadoReembolso;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.model.enums.MetodoPago;
import com.sm_sport.repository.*;
import com.sm_sport.service.impl.ReembolsoServiceImpl;
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
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReembolsoService - Tests Unitarios")
class ReembolsoServiceTest {

    @Mock
    private SolicitudReembolsoRepository solicitudRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private ReembolsoMapper reembolsoMapper;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReembolsoServiceImpl reembolsoService;

    private ReembolsoRequest reembolsoRequest;
    private Cliente clienteMock;
    private Administrador adminMock;
    private Reserva reservaMock;
    private Servicio servicioMock;
    private Pago pagoMock;
    private SolicitudReembolso solicitudMock;
    private SolicitudReembolsoResponse solicitudResponseMock;

    @BeforeEach
    void setUp() {
        setupReembolsoRequest();
        setupClienteMock();
        setupAdminMock();
        setupServicioMock();
        setupReservaMock();
        setupPagoMock();
        setupSolicitudMock();
        setupSolicitudResponseMock();
    }

    // ==================== TESTS DE SOLICITAR REEMBOLSO ====================

    @Test
    @DisplayName("Solicitar reembolso - Exitoso con 7+ días anticipación (100%)")
    void testSolicitarReembolso_7DiasAnticipacion_Exitoso() {
        // Arrange
        String idCliente = "cliente123";
        reservaMock.setFechaReserva(LocalDate.now().plusDays(10)); // 10 días después

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.of(pagoMock));
        when(solicitudRepository.findByReservaIdReserva(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.empty());
        when(reembolsoMapper.toEntity(any(ReembolsoRequest.class)))
                .thenReturn(solicitudMock);
        when(solicitudRepository.save(any(SolicitudReembolso.class)))
                .thenReturn(solicitudMock);
        when(reembolsoMapper.toResponse(any(SolicitudReembolso.class)))
                .thenReturn(solicitudResponseMock);

        // Act
        SolicitudReembolsoResponse response = reembolsoService.solicitarReembolso(
                idCliente, reembolsoRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdSolicitud()).isEqualTo(solicitudMock.getIdSolicitud());
        assertThat(response.getEstado()).isEqualTo(EstadoReembolso.SOLICITADO);

        // Verify
        verify(clienteRepository).findById(idCliente);
        // ✅ AHORA (permite múltiples llamadas)
        verify(reservaRepository, atLeastOnce()).findById(reembolsoRequest.getIdReserva());
        verify(pagoRepository, atLeastOnce()).findByReservaIdReserva(reembolsoRequest.getIdReserva());
        verify(solicitudRepository).save(any(SolicitudReembolso.class));
        verify(reservaRepository).save(any(Reserva.class));
        verify(notificacionService).notificarSistema(anyString(), anyString(), anyString());
        verify(emailService).enviarEmailCancelacion(anyString(), anyString());
    }

    @Test
    @DisplayName("Solicitar reembolso - Cliente no encontrado - Lanza ResourceNotFoundException")
    void testSolicitarReembolso_ClienteNoEncontrado_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente_inexistente";

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reembolsoService.solicitarReembolso(idCliente, reembolsoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente no encontrado");

        // Verify
        verify(clienteRepository).findById(idCliente);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("Solicitar reembolso - Reserva no encontrada - Lanza ResourceNotFoundException")
    void testSolicitarReembolso_ReservaNoEncontrada_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente123";

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reembolsoService.solicitarReembolso(idCliente, reembolsoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reserva no encontrada");

        // Verify
        verify(reservaRepository).findById(reembolsoRequest.getIdReserva());
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("Solicitar reembolso - Reserva no pertenece al cliente - Lanza BusinessException")
    void testSolicitarReembolso_ReservaNoPerteneceAlCliente_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente123";
        Cliente otroCliente = new Cliente();
        otroCliente.setIdUsuario("otro_cliente");
        reservaMock.setCliente(otroCliente);

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));

        // Act & Assert
        assertThatThrownBy(() -> reembolsoService.solicitarReembolso(idCliente, reembolsoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La reserva no pertenece al cliente");

        // Verify
        verify(solicitudRepository, never()).save(any());

        // Restaurar
        reservaMock.setCliente(clienteMock);
    }

    @Test
    @DisplayName("Solicitar reembolso - Reserva no confirmada - Lanza BusinessException")
    void testSolicitarReembolso_ReservaNoConfirmada_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente123";
        reservaMock.setEstado(EstadoReserva.FINALIZADA);

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));

        // Act & Assert
        assertThatThrownBy(() -> reembolsoService.solicitarReembolso(idCliente, reembolsoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo se pueden solicitar reembolsos para reservas confirmadas");

        // Verify
        verify(solicitudRepository, never()).save(any());

        // Restaurar
        reservaMock.setEstado(EstadoReserva.CONFIRMADA);
    }

    @Test
    @DisplayName("Solicitar reembolso - Pago no aprobado - Lanza BusinessException")
    void testSolicitarReembolso_PagoNoAprobado_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente123";
        pagoMock.setEstadoPago(EstadoPago.PENDIENTE);

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.of(pagoMock));

        // Act & Assert
        assertThatThrownBy(() -> reembolsoService.solicitarReembolso(idCliente, reembolsoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El pago de la reserva no está aprobado");

        // Verify
        verify(solicitudRepository, never()).save(any());

        // Restaurar
        pagoMock.setEstadoPago(EstadoPago.APROBADO);
    }

    @Test
    @DisplayName("Solicitar reembolso - Ya existe solicitud - Lanza BusinessException")
    void testSolicitarReembolso_YaExisteSolicitud_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente123";

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.of(pagoMock));
        when(solicitudRepository.findByReservaIdReserva(reembolsoRequest.getIdReserva()))
                .thenReturn(Optional.of(solicitudMock));

        // Act & Assert
        assertThatThrownBy(() -> reembolsoService.solicitarReembolso(idCliente, reembolsoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya existe una solicitud de reembolso");

        // Verify
        verify(solicitudRepository, never()).save(any());
    }

    // ==================== TESTS DE CALCULAR MONTO REEMBOLSO ====================

    @Test
    @DisplayName("Calcular monto reembolso - 7+ días anticipación - Retorna 100%")
    void testCalcularMontoReembolso_7DiasAnticipacion_Retorna100Porciento() {
        // Arrange
        String idReserva = "reserva123";
        reservaMock.setFechaReserva(LocalDate.now().plusDays(10));
        BigDecimal montoOriginal = new BigDecimal("50000");
        pagoMock.setMonto(montoOriginal);

        when(reservaRepository.findById(idReserva))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(idReserva))
                .thenReturn(Optional.of(pagoMock));

        // Act
        BigDecimal montoReembolso = reembolsoService.calcularMontoReembolso(idReserva);

        // Assert
        assertThat(montoReembolso).isEqualByComparingTo(montoOriginal); // 100%
        assertThat(montoReembolso).isEqualByComparingTo(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("Calcular monto reembolso - 3-6 días anticipación - Retorna 90%")
    void testCalcularMontoReembolso_3a6DiasAnticipacion_Retorna90Porciento() {
        // Arrange
        String idReserva = "reserva123";
        reservaMock.setFechaReserva(LocalDate.now().plusDays(5)); // 5 días
        BigDecimal montoOriginal = new BigDecimal("50000");
        pagoMock.setMonto(montoOriginal);

        when(reservaRepository.findById(idReserva))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(idReserva))
                .thenReturn(Optional.of(pagoMock));

        // Act
        BigDecimal montoReembolso = reembolsoService.calcularMontoReembolso(idReserva);

        // Assert
        BigDecimal esperado = new BigDecimal("45000.00"); // 90% de 50000
        assertThat(montoReembolso).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("Calcular monto reembolso - Menos de 3 días - Retorna 80%")
    void testCalcularMontoReembolso_MenosDe3Dias_Retorna80Porciento() {
        // Arrange
        String idReserva = "reserva123";
        reservaMock.setFechaReserva(LocalDate.now().plusDays(2)); // 2 días
        BigDecimal montoOriginal = new BigDecimal("50000");
        pagoMock.setMonto(montoOriginal);

        when(reservaRepository.findById(idReserva))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(idReserva))
                .thenReturn(Optional.of(pagoMock));

        // Act
        BigDecimal montoReembolso = reembolsoService.calcularMontoReembolso(idReserva);

        // Assert
        BigDecimal esperado = new BigDecimal("40000.00"); // 80% de 50000
        assertThat(montoReembolso).isEqualByComparingTo(esperado);
    }

    // ==================== TESTS DE APROBAR REEMBOLSO ====================

    @Test
    @DisplayName("Aprobar reembolso - Exitoso")
    void testAprobarReembolso_Exitoso() {
        // Arrange
        String idSolicitud = "solicitud123";
        String idAdmin = "admin123";

        // Crear respuesta específica para este test
        SolicitudReembolsoResponse responseAprobado = SolicitudReembolsoResponse.builder()
                .idSolicitud("solicitud123")
                .idReserva("reserva123")
                .montoReembolso(new BigDecimal("50000"))
                .estado(EstadoReembolso.APROBADO)  // 👈 APROBADO para este test
                .idCliente("cliente123")
                .nombreCliente("Juan Perez")
                .build();

        when(administradorRepository.findById(idAdmin))
                .thenReturn(Optional.of(adminMock));
        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitudMock));
        when(pagoRepository.findByReservaIdReserva(anyString()))
                .thenReturn(Optional.of(pagoMock));
        when(solicitudRepository.save(any(SolicitudReembolso.class)))
                .thenReturn(solicitudMock);
        when(reembolsoMapper.toResponse(any(SolicitudReembolso.class)))
                .thenReturn(responseAprobado);



        // Act
        SolicitudReembolsoResponse response = reembolsoService.aprobarReembolso(
                idSolicitud, idAdmin);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoReembolso.APROBADO);

        // Verify
        verify(administradorRepository).findById(idAdmin);
        verify(solicitudRepository).findById(idSolicitud);
        verify(solicitudRepository).save(any(SolicitudReembolso.class));
        verify(pagoRepository).save(any(Pago.class));
        verify(notificacionService).notificarSistema(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Aprobar reembolso - Admin no encontrado - Lanza ResourceNotFoundException")
    void testAprobarReembolso_AdminNoEncontrado_LanzaExcepcion() {
        // Arrange
        String idSolicitud = "solicitud123";
        String idAdmin = "admin_inexistente";

        when(administradorRepository.findById(idAdmin))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reembolsoService.aprobarReembolso(idSolicitud, idAdmin))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Administrador no encontrado");

        // Verify
        verify(administradorRepository).findById(idAdmin);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("Aprobar reembolso - Solicitud ya aprobada - Lanza BusinessException")
    void testAprobarReembolso_SolicitudYaAprobada_LanzaExcepcion() {
        // Arrange
        String idSolicitud = "solicitud123";
        String idAdmin = "admin123";
        solicitudMock.setEstado(EstadoReembolso.APROBADO);

        when(administradorRepository.findById(idAdmin))
                .thenReturn(Optional.of(adminMock));
        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitudMock));

        // Act & Assert
        assertThatThrownBy(() -> reembolsoService.aprobarReembolso(idSolicitud, idAdmin))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya fue aprobada");

        // Verify
        verify(solicitudRepository, never()).save(any());

        // Restaurar
        solicitudMock.setEstado(EstadoReembolso.SOLICITADO);
    }

    // ==================== TESTS DE RECHAZAR REEMBOLSO ====================

    @Test
    @DisplayName("Rechazar reembolso - Exitoso")
    void testRechazarReembolso_Exitoso() {
        // Arrange
        String idSolicitud = "solicitud123";
        String idAdmin = "admin123";
        String motivo = "La reserva ya fue utilizada";

        when(administradorRepository.findById(idAdmin))
                .thenReturn(Optional.of(adminMock));
        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitudMock));
        when(solicitudRepository.save(any(SolicitudReembolso.class)))
                .thenReturn(solicitudMock);
        when(reembolsoMapper.toResponse(any(SolicitudReembolso.class)))
                .thenReturn(solicitudResponseMock);

        // Act
        SolicitudReembolsoResponse response = reembolsoService.rechazarReembolso(
                idSolicitud, idAdmin, motivo);

        // Assert
        assertThat(response).isNotNull();

        // Verify
        verify(administradorRepository).findById(idAdmin);
        verify(solicitudRepository).findById(idSolicitud);
        verify(solicitudRepository).save(any(SolicitudReembolso.class));
        verify(reservaRepository).save(any(Reserva.class));
        verify(notificacionService).notificarSistema(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Rechazar reembolso - Sin motivo - Lanza BusinessException")
    void testRechazarReembolso_SinMotivo_LanzaExcepcion() {
        // Arrange
        String idSolicitud = "solicitud123";
        String idAdmin = "admin123";
        String motivoVacio = "";

        when(administradorRepository.findById(idAdmin))
                .thenReturn(Optional.of(adminMock));
        when(solicitudRepository.findById(idSolicitud))
                .thenReturn(Optional.of(solicitudMock));

        // Act & Assert
        assertThatThrownBy(() -> reembolsoService.rechazarReembolso(
                idSolicitud, idAdmin, motivoVacio))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Debe proporcionar un motivo");

        // Verify
        verify(solicitudRepository, never()).save(any());
    }

    // ==================== TESTS DE LISTAR ====================

    @Test
    @DisplayName("Listar por cliente - Exitoso")
    void testListarPorCliente_Exitoso() {
        // Arrange
        String idCliente = "cliente123";
        List<SolicitudReembolso> solicitudesMock = Arrays.asList(solicitudMock, solicitudMock);
        List<SolicitudReembolsoResponse> responsesMock = Arrays.asList(
                solicitudResponseMock, solicitudResponseMock);

        when(clienteRepository.existsById(idCliente))
                .thenReturn(true);
        when(solicitudRepository.findByClienteIdUsuario(idCliente))
                .thenReturn(solicitudesMock);
        when(reembolsoMapper.toResponseList(solicitudesMock))
                .thenReturn(responsesMock);

        // Act
        List<SolicitudReembolsoResponse> response = reembolsoService.listarPorCliente(idCliente);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        // Verify
        verify(clienteRepository).existsById(idCliente);
        verify(solicitudRepository).findByClienteIdUsuario(idCliente);
    }

    @Test
    @DisplayName("Listar pendientes - Exitoso")
    void testListarPendientes_Exitoso() {
        // Arrange
        List<SolicitudReembolso> solicitudesMock = Arrays.asList(solicitudMock);
        List<SolicitudReembolsoResponse> responsesMock = Arrays.asList(solicitudResponseMock);

        when(solicitudRepository.findSolicitudesPendientes())
                .thenReturn(solicitudesMock);
        when(reembolsoMapper.toResponseList(solicitudesMock))
                .thenReturn(responsesMock);

        // Act
        List<SolicitudReembolsoResponse> response = reembolsoService.listarPendientes();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);

        // Verify
        verify(solicitudRepository).findSolicitudesPendientes();
    }

    // ==================== MÉTODOS DE SETUP ====================

    private void setupReembolsoRequest() {
        reembolsoRequest = new ReembolsoRequest();
        reembolsoRequest.setIdReserva("reserva123");
        reembolsoRequest.setMotivoCancelacion("Surgió un imprevisto familiar");
    }

    private void setupClienteMock() {
        clienteMock = new Cliente();
        clienteMock.setIdUsuario("cliente123");
        clienteMock.setNombre("Juan Perez");
        clienteMock.setCorreo("juan.perez@test.com");
    }

    private void setupAdminMock() {
        adminMock = new Administrador();
        adminMock.setIdUsuario("admin123");
        adminMock.setNombre("Admin Sistema");
        adminMock.setNivelAcceso("SUPER_ADMIN");
    }

    private void setupServicioMock() {
        servicioMock = new Servicio();
        servicioMock.setIdServicio("servicio123");
        servicioMock.setNombre("Cancha de Fútbol 5");
        servicioMock.setDeporte("Fútbol");
        servicioMock.setPrecio(new BigDecimal("50000"));
    }

    private void setupReservaMock() {
        reservaMock = new Reserva();
        reservaMock.setIdReserva("reserva123");
        reservaMock.setCliente(clienteMock);
        reservaMock.setFechaReserva(LocalDate.now().plusDays(10));
        reservaMock.setHoraReserva(LocalTime.of(10, 0));
        reservaMock.setEstado(EstadoReserva.CONFIRMADA);
        reservaMock.setCostoTotal(new BigDecimal("50000"));
        reservaMock.setServicio(servicioMock);  // 👈 Esta línea
    }

    private void setupPagoMock() {
        pagoMock = new Pago();
        pagoMock.setIdPago("pago123");
        pagoMock.setCliente(clienteMock);
        pagoMock.setReserva(reservaMock);
        pagoMock.setMonto(new BigDecimal("50000"));
        pagoMock.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        pagoMock.setEstadoPago(EstadoPago.APROBADO);
        pagoMock.setReferenciaPago("PAY-ABC123");
        pagoMock.setFechaPago(LocalDateTime.now());
        pagoMock.setFechaAprobacion(LocalDateTime.now());
    }

    private void setupSolicitudMock() {
        solicitudMock = new SolicitudReembolso();
        solicitudMock.setIdSolicitud("solicitud123");
        solicitudMock.setReserva(reservaMock);
        solicitudMock.setCliente(clienteMock);
        solicitudMock.setMontoReembolso(new BigDecimal("50000"));
        solicitudMock.setMotivoCancelacion("Surgió un imprevisto familiar");
        solicitudMock.setEstado(EstadoReembolso.SOLICITADO);
        solicitudMock.setFechaSolicitud(LocalDateTime.now());
    }

    private void setupSolicitudResponseMock() {
        solicitudResponseMock = SolicitudReembolsoResponse.builder()
                .idSolicitud("solicitud123")
                .idReserva("reserva123")
                .montoReembolso(new BigDecimal("50000"))
                .motivoCancelacion("Surgió un imprevisto familiar")
                .fechaSolicitud(LocalDateTime.now())
                .estado(EstadoReembolso.SOLICITADO)  // ✅ CAMBIAR A SOLICITADO
                .idCliente("cliente123")
                .nombreCliente("Juan Perez")
                .build();
    }
}