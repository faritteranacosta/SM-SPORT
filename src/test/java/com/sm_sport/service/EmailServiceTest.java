package com.sm_sport.service;

import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.EstadoPago;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.model.enums.MetodoPago;
import com.sm_sport.repository.PagoRepository;
import com.sm_sport.repository.ReservaRepository;
import com.sm_sport.service.impl.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService - Tests Unitarios")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    private Reserva reservaMock;
    private Pago pagoMock;
    private Cliente clienteMock;
    private Proveedor proveedorMock;
    private Servicio servicioMock;
    private UbicacionServicio ubicacionMock;

    @BeforeEach
    void setUp() {
        // Configurar valores de propiedades
        ReflectionTestUtils.setField(emailService, "appName", "SM Sport");
        ReflectionTestUtils.setField(emailService, "emailFrom", "noreply@smsport.com");
        ReflectionTestUtils.setField(emailService, "emailFromName", "SM Sport - Santa Marta");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(emailService, "passwordResetPath", "/reset-password");

        setupClienteMock();
        setupProveedorMock();
        setupUbicacionMock();
        setupServicioMock();
        setupReservaMock();
        setupPagoMock();
    }

    // ==================== TESTS DE ENVIAR EMAIL REGISTRO ====================

    @Test
    @DisplayName("Enviar email de registro - Exitoso")
    void testEnviarEmailRegistro_Exitoso() throws MessagingException, UnsupportedEncodingException {
        // Arrange
        String destinatario = "juan.perez@test.com";
        String nombre = "Juan Pérez";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.enviarEmailRegistro(destinatario, nombre);

        // Esperar un poco para que el método asíncrono se ejecute
        sleep(100);

        // Assert & Verify
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar email de registro - Error en envío - No lanza excepción")
    void testEnviarEmailRegistro_ErrorEnEnvio_NoLanzaExcepcion() {
        // Arrange
        String destinatario = "juan.perez@test.com";
        String nombre = "Juan Pérez";

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Error de conexión"));

        // Act & Assert - No debe lanzar excepción
        assertThatCode(() -> emailService.enviarEmailRegistro(destinatario, nombre))
                .doesNotThrowAnyException();

        // Verify
        verify(mailSender).createMimeMessage();
    }

    // ==================== TESTS DE CONFIRMACIÓN DE RESERVA ====================

    @Test
    @DisplayName("Enviar email confirmación reserva - Exitoso")
    void testEnviarEmailConfirmacionReserva_Exitoso() {
        // Arrange
        String destinatario = "cliente@test.com";
        String idReserva = "reserva123";

        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaMock));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.enviarEmailConfirmacionReserva(destinatario, idReserva);

        // Esperar ejecución asíncrona
        sleep(100);

        // Assert & Verify
        verify(reservaRepository).findById(idReserva);
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar email confirmación reserva - Reserva no encontrada - No lanza excepción")
    void testEnviarEmailConfirmacionReserva_ReservaNoEncontrada_NoLanzaExcepcion() {
        // Arrange
        String destinatario = "cliente@test.com";
        String idReserva = "reserva_inexistente";

        when(reservaRepository.findById(idReserva)).thenReturn(Optional.empty());

        // Act & Assert - No debe lanzar excepción (método asíncrono maneja el error)
        assertThatCode(() -> emailService.enviarEmailConfirmacionReserva(destinatario, idReserva))
                .doesNotThrowAnyException();

        // Esperar ejecución asíncrona
        sleep(100);

        // Verify
        verify(reservaRepository).findById(idReserva);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar email confirmación reserva - Error al enviar - No lanza excepción")
    void testEnviarEmailConfirmacionReserva_ErrorAlEnviar_NoLanzaExcepcion() {
        // Arrange
        String destinatario = "cliente@test.com";
        String idReserva = "reserva123";

        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaMock));
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Error SMTP"));

        // Act & Assert
        assertThatCode(() -> emailService.enviarEmailConfirmacionReserva(destinatario, idReserva))
                .doesNotThrowAnyException();

        // Esperar ejecución asíncrona
        sleep(100);

        // Verify
        verify(reservaRepository).findById(idReserva);
        verify(mailSender).createMimeMessage();
    }

    // ==================== TESTS DE CANCELACIÓN ====================

    @Test
    @DisplayName("Enviar email de cancelación - Exitoso")
    void testEnviarEmailCancelacion_Exitoso() {
        // Arrange
        String destinatario = "cliente@test.com";
        String idReserva = "reserva123";

        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reservaMock));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.enviarEmailCancelacion(destinatario, idReserva);

        // Esperar ejecución asíncrona
        sleep(100);

        // Assert & Verify
        verify(reservaRepository).findById(idReserva);
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar email de cancelación - Reserva no encontrada - No lanza excepción")
    void testEnviarEmailCancelacion_ReservaNoEncontrada_NoLanzaExcepcion() {
        // Arrange
        String destinatario = "cliente@test.com";
        String idReserva = "reserva_inexistente";

        when(reservaRepository.findById(idReserva)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatCode(() -> emailService.enviarEmailCancelacion(destinatario, idReserva))
                .doesNotThrowAnyException();

        // Esperar ejecución asíncrona
        sleep(100);

        // Verify
        verify(reservaRepository).findById(idReserva);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ==================== TESTS DE RECUPERACIÓN DE CONTRASEÑA ====================

    @Test
    @DisplayName("Enviar email de recuperación - Exitoso")
    void testEnviarEmailRecuperacion_Exitoso() {
        // Arrange
        String destinatario = "usuario@test.com";
        String token = "abc123def456";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.enviarEmailRecuperacion(destinatario, token);

        // Esperar ejecución asíncrona
        sleep(100);

        // Assert & Verify
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar email de recuperación - Error al enviar - No lanza excepción")
    void testEnviarEmailRecuperacion_ErrorAlEnviar_NoLanzaExcepcion() {
        // Arrange
        String destinatario = "usuario@test.com";
        String token = "abc123def456";

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Error de red"));

        // Act & Assert
        assertThatCode(() -> emailService.enviarEmailRecuperacion(destinatario, token))
                .doesNotThrowAnyException();

        // Esperar ejecución asíncrona
        sleep(100);

        // Verify
        verify(mailSender).createMimeMessage();
    }

    // ==================== TESTS DE COMPROBANTE DE PAGO ====================

    @Test
    @DisplayName("Enviar comprobante de pago - Exitoso")
    void testEnviarComprobantePago_Exitoso() {
        // Arrange
        String destinatario = "cliente@test.com";
        String idPago = "pago123";

        when(pagoRepository.findById(idPago)).thenReturn(Optional.of(pagoMock));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.enviarComprobantePago(destinatario, idPago);

        // Esperar ejecución asíncrona
        sleep(100);

        // Assert & Verify
        verify(pagoRepository).findById(idPago);
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar comprobante de pago - Pago no encontrado - No lanza excepción")
    void testEnviarComprobantePago_PagoNoEncontrado_NoLanzaExcepcion() {
        // Arrange
        String destinatario = "cliente@test.com";
        String idPago = "pago_inexistente";

        when(pagoRepository.findById(idPago)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatCode(() -> emailService.enviarComprobantePago(destinatario, idPago))
                .doesNotThrowAnyException();

        // Esperar ejecución asíncrona
        sleep(100);

        // Verify
        verify(pagoRepository).findById(idPago);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar comprobante de pago - Error al enviar - No lanza excepción")
    void testEnviarComprobantePago_ErrorAlEnviar_NoLanzaExcepcion() {
        // Arrange
        String destinatario = "cliente@test.com";
        String idPago = "pago123";

        when(pagoRepository.findById(idPago)).thenReturn(Optional.of(pagoMock));
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Error SMTP"));

        // Act & Assert
        assertThatCode(() -> emailService.enviarComprobantePago(destinatario, idPago))
                .doesNotThrowAnyException();

        // Esperar ejecución asíncrona
        sleep(100);

        // Verify
        verify(pagoRepository).findById(idPago);
        verify(mailSender).createMimeMessage();
    }

    // ==================== MÉTODOS DE SETUP ====================

    private void setupClienteMock() {
        clienteMock = new Cliente();
        clienteMock.setIdUsuario("cliente123");
        clienteMock.setNombre("Juan Pérez");
        clienteMock.setCorreo("juan.perez@test.com");
    }

    private void setupProveedorMock() {
        proveedorMock = new Proveedor();
        proveedorMock.setIdUsuario("proveedor123");
        proveedorMock.setNombre("Deportes Santa Marta");
        proveedorMock.setCorreo("proveedor@test.com");
    }

    private void setupUbicacionMock() {
        ubicacionMock = new UbicacionServicio();
        ubicacionMock.setIdUbicacion("ubicacion123");
        ubicacionMock.setCiudad("Santa Marta");
        ubicacionMock.setDepartamento("Magdalena");
        ubicacionMock.setDireccion("Calle 22 #5-45, Centro");
        ubicacionMock.setPais("Colombia");
    }

    private void setupServicioMock() {
        servicioMock = new Servicio();
        servicioMock.setIdServicio("servicio123");
        servicioMock.setNombre("Cancha de Fútbol 5");
        servicioMock.setDeporte("Fútbol");
        servicioMock.setPrecio(new BigDecimal("50000"));
        servicioMock.setProveedor(proveedorMock);
        servicioMock.setUbicacion(ubicacionMock);
    }

    private void setupReservaMock() {
        reservaMock = new Reserva();
        reservaMock.setIdReserva("reserva12345");
        reservaMock.setCliente(clienteMock);
        reservaMock.setServicio(servicioMock);
        reservaMock.setProveedor(proveedorMock);
        reservaMock.setFechaReserva(LocalDate.now().plusDays(5));
        reservaMock.setHoraReserva(LocalTime.of(15, 0));
        reservaMock.setEstado(EstadoReserva.CONFIRMADA);
        reservaMock.setCostoTotal(new BigDecimal("50000"));
        reservaMock.setFechaCreacion(LocalDateTime.now());
    }

    private void setupPagoMock() {
        pagoMock = new Pago();
        pagoMock.setIdPago("pago12345678");
        pagoMock.setCliente(clienteMock);
        pagoMock.setReserva(reservaMock);
        pagoMock.setMonto(new BigDecimal("50000"));
        pagoMock.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        pagoMock.setEstadoPago(EstadoPago.APROBADO);
        pagoMock.setReferenciaPago("PAY-ABC12345"); // Este está bien
        pagoMock.setFechaPago(LocalDateTime.now());
        pagoMock.setFechaAprobacion(LocalDateTime.now());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Helper para esperar ejecución de métodos asíncronos
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}