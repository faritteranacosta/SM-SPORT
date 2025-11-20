package com.sm_sport.service;

import com.sm_sport.dto.request.PagoRequest;
import com.sm_sport.dto.response.ComprobanteResponse;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PagoResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.PaymentException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.PagoMapper;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.EstadoPago;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.model.enums.MetodoPago;
import com.sm_sport.repository.*;
import com.sm_sport.service.impl.PagoServiceImpl;
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
@DisplayName("PagoService - Tests Unitarios")
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ComprobanteRepository comprobanteRepository;

    @Mock
    private PagoMapper pagoMapper;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private PagoRequest pagoTarjetaRequest;
    private PagoRequest pagoBilleteraRequest;
    private PagoRequest pagoTransferenciaRequest;
    private Cliente clienteMock;
    private Reserva reservaMock;
    private Pago pagoMock;
    private Comprobante comprobanteMock;
    private PagoResponse pagoResponseMock;
    private ComprobanteResponse comprobanteResponseMock;

    @BeforeEach
    void setUp() {
        setupPagoTarjetaRequest();
        setupPagoBilleteraRequest();
        setupPagoTransferenciaRequest();
        setupClienteMock();
        setupReservaMock();
        setupPagoMock();
        setupComprobanteMock();
        setupPagoResponseMock();
        setupComprobanteResponseMock();
    }

    // ==================== TESTS DE PROCESAR PAGO ====================

    @Test
    @DisplayName("Procesar pago con tarjeta - Exitoso")
    void testProcesarPagoConTarjeta_Exitoso() {
        // Arrange
        String idCliente = "cliente123";

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(pagoTarjetaRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(pagoTarjetaRequest.getIdReserva()))
                .thenReturn(Optional.empty());
        when(pagoMapper.toEntity(any(PagoRequest.class)))
                .thenReturn(pagoMock);
        when(pagoRepository.save(any(Pago.class)))
                .thenReturn(pagoMock);
        when(pagoMapper.toResponse(any(Pago.class)))
                .thenReturn(pagoResponseMock);

        // Act
        PagoResponse response = pagoService.procesarPago(idCliente, pagoTarjetaRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdPago()).isEqualTo(pagoMock.getIdPago());
        assertThat(response.getEstadoPago()).isEqualTo(EstadoPago.APROBADO);
        assertThat(response.getMonto()).isEqualTo(pagoMock.getMonto());

        // Verify
        verify(clienteRepository).findById(idCliente);
        verify(reservaRepository).findById(pagoTarjetaRequest.getIdReserva());
        verify(pagoRepository).findByReservaIdReserva(pagoTarjetaRequest.getIdReserva());
        verify(pagoMapper).toEntity(pagoTarjetaRequest);
        verify(pagoRepository, atLeastOnce()).save(any(Pago.class));
        verify(reservaRepository).save(any(Reserva.class));
        verify(comprobanteRepository).save(any(Comprobante.class));
    }

    @Test
    @DisplayName("Procesar pago con billetera digital - Exitoso")
    void testProcesarPagoConBilletera_Exitoso() {
        // Arrange
        String idCliente = "cliente123";

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(pagoBilleteraRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(pagoBilleteraRequest.getIdReserva()))
                .thenReturn(Optional.empty());
        when(pagoMapper.toEntity(any(PagoRequest.class)))
                .thenReturn(pagoMock);
        when(pagoRepository.save(any(Pago.class)))
                .thenReturn(pagoMock);
        when(pagoMapper.toResponse(any(Pago.class)))
                .thenReturn(pagoResponseMock);

        // Act
        PagoResponse response = pagoService.procesarPago(idCliente, pagoBilleteraRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstadoPago()).isEqualTo(EstadoPago.APROBADO);

        // Verify
        verify(clienteRepository).findById(idCliente);
        verify(reservaRepository).findById(pagoBilleteraRequest.getIdReserva());
        verify(pagoRepository, atLeastOnce()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Procesar pago - Cliente no encontrado - Lanza ResourceNotFoundException")
    void testProcesarPago_ClienteNoEncontrado_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente_inexistente";

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> pagoService.procesarPago(idCliente, pagoTarjetaRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cliente no encontrado");

        // Verify
        verify(clienteRepository).findById(idCliente);
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Procesar pago - Reserva no encontrada - Lanza ResourceNotFoundException")
    void testProcesarPago_ReservaNoEncontrada_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente123";

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(pagoTarjetaRequest.getIdReserva()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> pagoService.procesarPago(idCliente, pagoTarjetaRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reserva no encontrada");

        // Verify
        verify(clienteRepository).findById(idCliente);
        verify(reservaRepository).findById(pagoTarjetaRequest.getIdReserva());
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Procesar pago - Reserva no pertenece al cliente - Lanza BusinessException")
    void testProcesarPago_ReservaNoPerteneceAlCliente_LanzaExcepcion() {
        // Arrange
        String idClienteDiferente = "cliente_diferente";
        Cliente otroCliente = new Cliente();
        otroCliente.setIdUsuario("otro_cliente");

        when(clienteRepository.findById(idClienteDiferente))
                .thenReturn(Optional.of(otroCliente));
        when(reservaRepository.findById(pagoTarjetaRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));

        // Act & Assert
        assertThatThrownBy(() -> pagoService.procesarPago(idClienteDiferente, pagoTarjetaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La reserva no pertenece al cliente");

        // Verify
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Procesar pago - Reserva no está pendiente - Lanza BusinessException")
    void testProcesarPago_ReservaNoEstaPendiente_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente123";
        reservaMock.setEstado(EstadoReserva.CONFIRMADA);

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(pagoTarjetaRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));

        // Act & Assert
        assertThatThrownBy(() -> pagoService.procesarPago(idCliente, pagoTarjetaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La reserva no está en estado pendiente");

        // Verify
        verify(pagoRepository, never()).save(any());

        // Restaurar estado
        reservaMock.setEstado(EstadoReserva.PENDIENTE);
    }

    @Test
    @DisplayName("Procesar pago - Reserva ya tiene pago - Lanza BusinessException")
    void testProcesarPago_ReservaYaTienePago_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente123";

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(pagoTarjetaRequest.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(pagoTarjetaRequest.getIdReserva()))
                .thenReturn(Optional.of(pagoMock));

        // Act & Assert
        assertThatThrownBy(() -> pagoService.procesarPago(idCliente, pagoTarjetaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La reserva ya tiene un pago asociado");

        // Verify
        verify(pagoRepository).findByReservaIdReserva(pagoTarjetaRequest.getIdReserva());
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Procesar pago - Datos de pago inválidos - Lanza PaymentException")
    void testProcesarPago_DatosInvalidos_LanzaExcepcion() {
        // Arrange
        String idCliente = "cliente123";
        PagoRequest requestInvalido = PagoRequest.builder()
                .idReserva("reserva123")
                .metodoPago(MetodoPago.TARJETA_CREDITO)
                .numeroTarjeta(null) // Dato faltante
                .build();

        when(clienteRepository.findById(idCliente))
                .thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(requestInvalido.getIdReserva()))
                .thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByReservaIdReserva(requestInvalido.getIdReserva()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> pagoService.procesarPago(idCliente, requestInvalido))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Datos de pago inválidos");

        // Verify
        verify(pagoRepository, never()).save(any());
    }

    // ==================== TESTS DE OBTENER PAGO ====================

    @Test
    @DisplayName("Obtener pago por ID - Exitoso")
    void testObtenerPorId_Exitoso() {
        // Arrange
        String idPago = "pago123";

        when(pagoRepository.findById(idPago))
                .thenReturn(Optional.of(pagoMock));
        when(pagoMapper.toResponse(pagoMock))
                .thenReturn(pagoResponseMock);

        // Act
        PagoResponse response = pagoService.obtenerPorId(idPago);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdPago()).isEqualTo(pagoMock.getIdPago());

        // Verify
        verify(pagoRepository).findById(idPago);
        verify(pagoMapper).toResponse(pagoMock);
    }

    @Test
    @DisplayName("Obtener pago por ID - No encontrado - Lanza ResourceNotFoundException")
    void testObtenerPorId_NoEncontrado_LanzaExcepcion() {
        // Arrange
        String idPago = "pago_inexistente";

        when(pagoRepository.findById(idPago))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> pagoService.obtenerPorId(idPago))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pago no encontrado");

        // Verify
        verify(pagoRepository).findById(idPago);
        verify(pagoMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Obtener pago por reserva - Exitoso")
    void testObtenerPorReserva_Exitoso() {
        // Arrange
        String idReserva = "reserva123";

        when(pagoRepository.findByReservaIdReserva(idReserva))
                .thenReturn(Optional.of(pagoMock));
        when(pagoMapper.toResponse(pagoMock))
                .thenReturn(pagoResponseMock);

        // Act
        PagoResponse response = pagoService.obtenerPorReserva(idReserva);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdPago()).isEqualTo(pagoMock.getIdPago());

        // Verify
        verify(pagoRepository).findByReservaIdReserva(idReserva);
        verify(pagoMapper).toResponse(pagoMock);
    }

    @Test
    @DisplayName("Obtener pago por reserva - No encontrado - Lanza ResourceNotFoundException")
    void testObtenerPorReserva_NoEncontrado_LanzaExcepcion() {
        // Arrange
        String idReserva = "reserva_sin_pago";

        when(pagoRepository.findByReservaIdReserva(idReserva))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> pagoService.obtenerPorReserva(idReserva))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pago no encontrado");

        // Verify
        verify(pagoRepository).findByReservaIdReserva(idReserva);
    }

    // ==================== TESTS DE LISTAR PAGOS ====================

    @Test
    @DisplayName("Listar pagos por cliente - Exitoso")
    void testListarPorCliente_Exitoso() {
        // Arrange
        String idCliente = "cliente123";
        List<Pago> pagosMock = Arrays.asList(pagoMock, pagoMock);
        List<PagoResponse> responseMock = Arrays.asList(pagoResponseMock, pagoResponseMock);

        when(pagoRepository.findByClienteIdUsuario(idCliente))
                .thenReturn(pagosMock);
        when(pagoMapper.toResponseList(pagosMock))
                .thenReturn(responseMock);

        // Act
        List<PagoResponse> response = pagoService.listarPorCliente(idCliente);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        // Verify
        verify(pagoRepository).findByClienteIdUsuario(idCliente);
        verify(pagoMapper).toResponseList(pagosMock);
    }

    @Test
    @DisplayName("Listar pagos por cliente - Lista vacía")
    void testListarPorCliente_ListaVacia() {
        // Arrange
        String idCliente = "cliente_sin_pagos";

        when(pagoRepository.findByClienteIdUsuario(idCliente))
                .thenReturn(List.of());
        when(pagoMapper.toResponseList(anyList()))
                .thenReturn(List.of());

        // Act
        List<PagoResponse> response = pagoService.listarPorCliente(idCliente);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response).isEmpty();

        // Verify
        verify(pagoRepository).findByClienteIdUsuario(idCliente);
    }

    // ==================== TESTS DE GENERAR COMPROBANTE ====================

    @Test
    @DisplayName("Generar comprobante - Exitoso")
    void testGenerarComprobante_Exitoso() {
        // Arrange
        String idPago = "pago123";
        pagoMock.setEstadoPago(EstadoPago.APROBADO);

        when(pagoRepository.findById(idPago))
                .thenReturn(Optional.of(pagoMock));
        when(comprobanteRepository.findByPagoIdPago(idPago))
                .thenReturn(Optional.empty());
        when(comprobanteRepository.save(any(Comprobante.class)))
                .thenReturn(comprobanteMock);
        when(pagoMapper.toComprobanteResponse(any(Comprobante.class)))
                .thenReturn(comprobanteResponseMock);

        // Act
        ComprobanteResponse response = pagoService.generarComprobante(idPago);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIdComprobante()).isEqualTo(comprobanteMock.getIdComprobante());

        // Verify
        verify(pagoRepository).findById(idPago);
        verify(comprobanteRepository).findByPagoIdPago(idPago);
        verify(comprobanteRepository).save(any(Comprobante.class));
    }

    @Test
    @DisplayName("Generar comprobante - Ya existe - Retorna existente")
    void testGenerarComprobante_YaExiste_RetornaExistente() {
        // Arrange
        String idPago = "pago123";
        pagoMock.setEstadoPago(EstadoPago.APROBADO);

        when(pagoRepository.findById(idPago))
                .thenReturn(Optional.of(pagoMock));
        when(comprobanteRepository.findByPagoIdPago(idPago))
                .thenReturn(Optional.of(comprobanteMock));
        when(pagoMapper.toComprobanteResponse(comprobanteMock))
                .thenReturn(comprobanteResponseMock);

        // Act
        ComprobanteResponse response = pagoService.generarComprobante(idPago);

        // Assert
        assertThat(response).isNotNull();

        // Verify
        verify(pagoRepository).findById(idPago);
        verify(comprobanteRepository).findByPagoIdPago(idPago);
        verify(comprobanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Generar comprobante - Pago no aprobado - Lanza BusinessException")
    void testGenerarComprobante_PagoNoAprobado_LanzaExcepcion() {
        // Arrange
        String idPago = "pago123";
        pagoMock.setEstadoPago(EstadoPago.PENDIENTE);

        when(pagoRepository.findById(idPago))
                .thenReturn(Optional.of(pagoMock));

        // Act & Assert
        assertThatThrownBy(() -> pagoService.generarComprobante(idPago))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Solo se pueden generar comprobantes de pagos aprobados");

        // Verify
        verify(pagoRepository).findById(idPago);
        verify(comprobanteRepository, never()).save(any());

        // Restaurar estado
        pagoMock.setEstadoPago(EstadoPago.APROBADO);
    }

    // ==================== TESTS DE REEMBOLSO ====================

    @Test
    @DisplayName("Procesar reembolso - Exitoso")
    void testProcesarReembolso_Exitoso() {
        // Arrange
        String idPago = "pago123";
        pagoMock.setEstadoPago(EstadoPago.APROBADO);

        when(pagoRepository.findById(idPago))
                .thenReturn(Optional.of(pagoMock));
        when(pagoRepository.save(any(Pago.class)))
                .thenReturn(pagoMock);

        // Act
        MessageResponse response = pagoService.procesarReembolso(idPago);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Reembolso procesado exitosamente");

        // Verify
        verify(pagoRepository).findById(idPago);
        verify(pagoRepository).save(pagoMock);
    }

    @Test
    @DisplayName("Procesar reembolso - Pago no aprobado - Lanza BusinessException")
    void testProcesarReembolso_PagoNoAprobado_LanzaExcepcion() {
        // Arrange
        String idPago = "pago123";
        pagoMock.setEstadoPago(EstadoPago.PENDIENTE);

        when(pagoRepository.findById(idPago))
                .thenReturn(Optional.of(pagoMock));

        // Act & Assert
        assertThatThrownBy(() -> pagoService.procesarReembolso(idPago))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Solo se pueden reembolsar pagos aprobados");

        // Verify
        verify(pagoRepository).findById(idPago);
        verify(pagoRepository, never()).save(any());

        // Restaurar estado
        pagoMock.setEstadoPago(EstadoPago.APROBADO);
    }

    // ==================== TESTS DE VALIDACIÓN ====================

    @Test
    @DisplayName("Validar datos pago con tarjeta - Datos completos - Retorna true")
    void testValidarDatosPago_TarjetaCompleta_RetornaTrue() {
        // Act
        boolean resultado = pagoService.validarDatosPago(pagoTarjetaRequest);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Validar datos pago con tarjeta - Datos incompletos - Retorna false")
    void testValidarDatosPago_TarjetaIncompleta_RetornaFalse() {
        // Arrange
        PagoRequest requestIncompleto = PagoRequest.builder()
                .metodoPago(MetodoPago.TARJETA_CREDITO)
                .numeroTarjeta("4111111111111111")
                .nombreTitular(null) // Falta nombre
                .build();

        // Act
        boolean resultado = pagoService.validarDatosPago(requestIncompleto);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Validar datos pago con billetera - Datos completos - Retorna true")
    void testValidarDatosPago_BilleteraCompleta_RetornaTrue() {
        // Act
        boolean resultado = pagoService.validarDatosPago(pagoBilleteraRequest);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Validar datos pago con billetera - Sin email - Retorna false")
    void testValidarDatosPago_BilleteraSinEmail_RetornaFalse() {
        // Arrange
        PagoRequest requestSinEmail = PagoRequest.builder()
                .metodoPago(MetodoPago.BILLETERA_DIGITAL)
                .emailBilletera(null)
                .build();

        // Act
        boolean resultado = pagoService.validarDatosPago(requestSinEmail);

        // Assert
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Validar datos pago con transferencia - Retorna true")
    void testValidarDatosPago_Transferencia_RetornaTrue() {
        // Act
        boolean resultado = pagoService.validarDatosPago(pagoTransferenciaRequest);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Validar datos pago - Sin método de pago - Retorna false")
    void testValidarDatosPago_SinMetodo_RetornaFalse() {
        // Arrange
        PagoRequest requestSinMetodo = PagoRequest.builder()
                .metodoPago(null)
                .build();

        // Act
        boolean resultado = pagoService.validarDatosPago(requestSinMetodo);

        // Assert
        assertThat(resultado).isFalse();
    }

    // ==================== MÉTODOS DE SETUP ====================

    private void setupPagoTarjetaRequest() {
        pagoTarjetaRequest = PagoRequest.builder()
                .idReserva("reserva123")
                .metodoPago(MetodoPago.TARJETA_CREDITO)
                .numeroTarjeta("4111111111111111")
                .nombreTitular("Juan Perez")
                .fechaExpiracion("12/25")
                .cvv("123")
                .build();
    }

    private void setupPagoBilleteraRequest() {
        pagoBilleteraRequest = PagoRequest.builder()
                .idReserva("reserva123")
                .metodoPago(MetodoPago.BILLETERA_DIGITAL)
                .emailBilletera("juan.perez@test.com")
                .build();
    }

    private void setupPagoTransferenciaRequest() {
        pagoTransferenciaRequest = PagoRequest.builder()
                .idReserva("reserva123")
                .metodoPago(MetodoPago.TRANSFERENCIA_BANCARIA)
                .build();
    }

    private void setupClienteMock() {
        clienteMock = new Cliente();
        clienteMock.setIdUsuario("cliente123");
        clienteMock.setNombre("Juan Perez");
        clienteMock.setCorreo("juan.perez@test.com");
    }

    private void setupReservaMock() {
        reservaMock = new Reserva();
        reservaMock.setIdReserva("reserva123");
        reservaMock.setCliente(clienteMock);
        reservaMock.setFechaReserva(LocalDate.now().plusDays(5));
        reservaMock.setHoraReserva(LocalTime.of(10, 0));
        reservaMock.setEstado(EstadoReserva.PENDIENTE);
        reservaMock.setCostoTotal(new BigDecimal("50000"));
    }

    private void setupPagoMock() {
        pagoMock = new Pago();
        pagoMock.setIdPago("pago123");
        pagoMock.setCliente(clienteMock);
        pagoMock.setReserva(reservaMock);
        pagoMock.setMonto(new BigDecimal("50000"));
        pagoMock.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        pagoMock.setEstadoPago(EstadoPago.APROBADO);
        pagoMock.setReferenciaPago("PAY-ABC12345");
        pagoMock.setFechaPago(LocalDateTime.now());
        pagoMock.setFechaAprobacion(LocalDateTime.now());
    }

    private void setupComprobanteMock() {
        comprobanteMock = new Comprobante();
        comprobanteMock.setIdComprobante("comprobante123");
        comprobanteMock.setPago(pagoMock);
        comprobanteMock.setMonto(new BigDecimal("50000"));
        comprobanteMock.setDetalle("Comprobante de pago - Reserva #reserva123");
        comprobanteMock.setFormato("PDF");
        comprobanteMock.setUrlArchivo("/comprobantes/pago123.pdf");
        // fechaGeneracion se genera automáticamente con @CreatedDate
    }

    private void setupPagoResponseMock() {
        pagoResponseMock = PagoResponse.builder()
                .idPago("pago123")
                .idReserva("reserva123")
                .monto(new BigDecimal("50000"))
                .metodoPago(MetodoPago.TARJETA_CREDITO)
                .estadoPago(EstadoPago.APROBADO)
                .referenciaPago("PAY-ABC12345")
                .fechaPago(LocalDateTime.now())
                .fechaAprobacion(LocalDateTime.now())
                .proveedorPasarela("Stripe")
                .build();
    }

    private void setupComprobanteResponseMock() {
        comprobanteResponseMock = ComprobanteResponse.builder()
                .idComprobante("comprobante123")
                .idPago("pago123")
                .monto(new BigDecimal("50000"))
                .detalle("Comprobante de pago - Reserva #reserva123")
                .formato("PDF")
                .urlArchivo("/comprobantes/pago123.pdf")
                .build();
    }
}