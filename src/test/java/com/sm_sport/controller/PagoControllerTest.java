package com.sm_sport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm_sport.dto.request.PagoRequest;
import com.sm_sport.dto.response.ComprobanteResponse;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PagoResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.PaymentException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.model.enums.EstadoPago;
import com.sm_sport.model.enums.MetodoPago;
import com.sm_sport.service.PagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests para PagoController")
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagoService pagoService;

    @MockBean
    private com.sm_sport.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.sm_sport.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private PagoRequest pagoRequest;
    private PagoResponse pagoResponse;
    private ComprobanteResponse comprobanteResponse;

    @BeforeEach
    void setUp() {
        // Request de pago
        pagoRequest = PagoRequest.builder()
                .idReserva("reserva-123")
                .metodoPago(MetodoPago.TARJETA_CREDITO)
                .numeroTarjeta("4111111111111111")
                .nombreTitular("Juan Pérez")
                .fechaExpiracion("12/25")
                .cvv("123")
                .build();

        // Response de pago
        pagoResponse = PagoResponse.builder()
                .idPago("pago-123")
                .idReserva("reserva-123")
                .monto(BigDecimal.valueOf(50000))
                .metodoPago(MetodoPago.TARJETA_CREDITO)
                .estadoPago(EstadoPago.APROBADO)
                .referenciaPago("PAY-ABC123")
                .fechaPago(LocalDateTime.now())
                .fechaAprobacion(LocalDateTime.now())
                .proveedorPasarela("STRIPE")
                .build();

        // Response de comprobante
        comprobanteResponse = ComprobanteResponse.builder()
                .idComprobante("comp-123")
                .idPago("pago-123")
                .monto(BigDecimal.valueOf(50000))
                .detalle("Comprobante de pago - Reserva #reserva-123")
                .formato("PDF")
                .urlArchivo("/comprobantes/pago-123.pdf")
                .fechaEmision(LocalDateTime.now())
                .build();
    }

    // ==================== TESTS DE PROCESAR PAGO ====================

    @Test
    @DisplayName("POST /api/v1/pagos - Procesar pago exitoso")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void procesarPago_Exitoso() throws Exception {
        // Arrange
        when(pagoService.procesarPago(anyString(), any(PagoRequest.class)))
                .thenReturn(pagoResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPago").value("pago-123"))
                .andExpect(jsonPath("$.idReserva").value("reserva-123"))
                .andExpect(jsonPath("$.estadoPago").value("APROBADO"))
                .andExpect(jsonPath("$.monto").value(50000))
                .andExpect(jsonPath("$.metodoPago").value("TARJETA_CREDITO"))
                .andExpect(jsonPath("$.referenciaPago").value("PAY-ABC123"))
                .andExpect(jsonPath("$.proveedorPasarela").value("STRIPE"));

        verify(pagoService, times(1)).procesarPago(eq("cliente-123"), any(PagoRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/pagos - Request inválido sin ID de reserva")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void procesarPago_RequestInvalidoSinIdReserva() throws Exception {
        // Arrange
        pagoRequest.setIdReserva(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(pagoService, never()).procesarPago(anyString(), any(PagoRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/pagos - Request inválido sin método de pago")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void procesarPago_RequestInvalidoSinMetodoPago() throws Exception {
        // Arrange
        pagoRequest.setMetodoPago(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(pagoService, never()).procesarPago(anyString(), any(PagoRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/pagos - Reserva no encontrada")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void procesarPago_ReservaNoEncontrada() throws Exception {
        // Arrange
        when(pagoService.procesarPago(anyString(), any(PagoRequest.class)))
                .thenThrow(new ResourceNotFoundException("Reserva no encontrada"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(pagoService, times(1)).procesarPago(anyString(), any(PagoRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/pagos - Reserva no pertenece al cliente")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void procesarPago_ReservaNoPertenece() throws Exception {
        // Arrange
        when(pagoService.procesarPago(anyString(), any(PagoRequest.class)))
                .thenThrow(new BusinessException("La reserva no pertenece al cliente"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(pagoService, times(1)).procesarPago(anyString(), any(PagoRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/pagos - Pago rechazado por pasarela")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void procesarPago_PagoRechazado() throws Exception {
        // Arrange
        when(pagoService.procesarPago(anyString(), any(PagoRequest.class)))
                .thenThrow(new PaymentException("El pago fue rechazado por la pasarela"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoRequest)))
                .andDo(print())
                .andExpect(status().isPaymentRequired());

        verify(pagoService, times(1)).procesarPago(anyString(), any(PagoRequest.class));
    }





    // ==================== TESTS DE OBTENER PAGO POR ID ====================

    @Test
    @DisplayName("GET /api/v1/pagos/{id} - Obtener pago exitoso (Cliente)")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void obtenerPagoPorId_ExitosoCliente() throws Exception {
        // Arrange
        when(pagoService.obtenerPorId(anyString())).thenReturn(pagoResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/pago-123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value("pago-123"))
                .andExpect(jsonPath("$.estadoPago").value("APROBADO"))
                .andExpect(jsonPath("$.monto").value(50000));

        verify(pagoService, times(1)).obtenerPorId("pago-123");
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} - Obtener pago exitoso (Administrador)")
    @WithMockUser(username = "admin-123", roles = "ADMINISTRADOR")
    void obtenerPagoPorId_ExitosoAdministrador() throws Exception {
        // Arrange
        when(pagoService.obtenerPorId(anyString())).thenReturn(pagoResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/pago-123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value("pago-123"));

        verify(pagoService, times(1)).obtenerPorId("pago-123");
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} - Pago no encontrado")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void obtenerPagoPorId_NoEncontrado() throws Exception {
        // Arrange
        when(pagoService.obtenerPorId(anyString()))
                .thenThrow(new ResourceNotFoundException("Pago no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/pago-inexistente"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(pagoService, times(1)).obtenerPorId("pago-inexistente");
    }


    // ==================== TESTS DE OBTENER PAGO POR RESERVA ====================

    @Test
    @DisplayName("GET /api/v1/pagos/reserva/{idReserva} - Obtener pago exitoso")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void obtenerPagoPorReserva_Exitoso() throws Exception {
        // Arrange
        when(pagoService.obtenerPorReserva(anyString())).thenReturn(pagoResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/reserva/reserva-123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value("pago-123"))
                .andExpect(jsonPath("$.idReserva").value("reserva-123"));

        verify(pagoService, times(1)).obtenerPorReserva("reserva-123");
    }

    @Test
    @DisplayName("GET /api/v1/pagos/reserva/{idReserva} - Pago no encontrado")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void obtenerPagoPorReserva_NoEncontrado() throws Exception {
        // Arrange
        when(pagoService.obtenerPorReserva(anyString()))
                .thenThrow(new ResourceNotFoundException("No se encontró pago para la reserva especificada"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/reserva/reserva-inexistente"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(pagoService, times(1)).obtenerPorReserva("reserva-inexistente");
    }

    @Test
    @DisplayName("GET /api/v1/pagos/reserva/{idReserva} - Acceso permitido para PROVEEDOR")
    @WithMockUser(username = "proveedor-123", roles = "PROVEEDOR")
    void obtenerPagoPorReserva_AccesoProveedor() throws Exception {
        // Arrange
        when(pagoService.obtenerPorReserva(anyString())).thenReturn(pagoResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/reserva/reserva-123"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(pagoService, times(1)).obtenerPorReserva("reserva-123");
    }

    // ==================== TESTS DE LISTAR MIS PAGOS ====================

    @Test
    @DisplayName("GET /api/v1/pagos/mis-pagos - Listar pagos exitoso")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void listarMisPagos_Exitoso() throws Exception {
        // Arrange
        PagoResponse pago1 = PagoResponse.builder()
                .idPago("pago-1")
                .monto(BigDecimal.valueOf(30000))
                .estadoPago(EstadoPago.APROBADO)
                .build();

        PagoResponse pago2 = PagoResponse.builder()
                .idPago("pago-2")
                .monto(BigDecimal.valueOf(50000))
                .estadoPago(EstadoPago.APROBADO)
                .build();

        List<PagoResponse> pagos = Arrays.asList(pago1, pago2);

        when(pagoService.listarPorCliente(anyString())).thenReturn(pagos);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/mis-pagos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idPago").value("pago-1"))
                .andExpect(jsonPath("$[1].idPago").value("pago-2"))
                .andExpect(jsonPath("$[0].monto").value(30000))
                .andExpect(jsonPath("$[1].monto").value(50000));

        verify(pagoService, times(1)).listarPorCliente("cliente-123");
    }

    @Test
    @DisplayName("GET /api/v1/pagos/mis-pagos - Lista vacía")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void listarMisPagos_ListaVacia() throws Exception {
        // Arrange
        when(pagoService.listarPorCliente(anyString())).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/mis-pagos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(pagoService, times(1)).listarPorCliente("cliente-123");
    }




    // ==================== TESTS DE OBTENER COMPROBANTE ====================

    @Test
    @DisplayName("GET /api/v1/pagos/{id}/comprobante - Obtener comprobante exitoso")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void obtenerComprobante_Exitoso() throws Exception {
        // Arrange
        when(pagoService.generarComprobante(anyString())).thenReturn(comprobanteResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/pago-123/comprobante"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idComprobante").value("comp-123"))
                .andExpect(jsonPath("$.idPago").value("pago-123"))
                .andExpect(jsonPath("$.monto").value(50000))
                .andExpect(jsonPath("$.formato").value("PDF"))
                .andExpect(jsonPath("$.urlArchivo").value("/comprobantes/pago-123.pdf"));

        verify(pagoService, times(1)).generarComprobante("pago-123");
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id}/comprobante - Pago no aprobado")
    @WithMockUser(username = "cliente-123", roles = "CLIENTE")
    void obtenerComprobante_PagoNoAprobado() throws Exception {
        // Arrange
        when(pagoService.generarComprobante(anyString()))
                .thenThrow(new BusinessException("Solo se pueden generar comprobantes de pagos aprobados"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/pago-123/comprobante"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(pagoService, times(1)).generarComprobante("pago-123");
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id}/comprobante - Acceso permitido para PROVEEDOR")
    @WithMockUser(username = "proveedor-123", roles = "PROVEEDOR")
    void obtenerComprobante_AccesoProveedor() throws Exception {
        // Arrange
        when(pagoService.generarComprobante(anyString())).thenReturn(comprobanteResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/pago-123/comprobante"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(pagoService, times(1)).generarComprobante("pago-123");
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id}/comprobante - Acceso permitido para ADMINISTRADOR")
    @WithMockUser(username = "admin-123", roles = "ADMINISTRADOR")
    void obtenerComprobante_AccesoAdministrador() throws Exception {
        // Arrange
        when(pagoService.generarComprobante(anyString())).thenReturn(comprobanteResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pagos/pago-123/comprobante"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(pagoService, times(1)).generarComprobante("pago-123");
    }

    // ==================== TESTS DE PROCESAR REEMBOLSO ====================

    @Test
    @DisplayName("POST /api/v1/pagos/{id}/reembolso - Procesar reembolso exitoso")
    @WithMockUser(username = "admin-123", roles = "ADMINISTRADOR")
    void procesarReembolso_Exitoso() throws Exception {
        // Arrange
        MessageResponse messageResponse = MessageResponse.success("Reembolso procesado exitosamente");
        when(pagoService.procesarReembolso(anyString())).thenReturn(messageResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos/pago-123/reembolso")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reembolso procesado exitosamente"));

        verify(pagoService, times(1)).procesarReembolso("pago-123");
    }

    @Test
    @DisplayName("POST /api/v1/pagos/{id}/reembolso - Pago no encontrado")
    @WithMockUser(username = "admin-123", roles = "ADMINISTRADOR")
    void procesarReembolso_PagoNoEncontrado() throws Exception {
        // Arrange
        when(pagoService.procesarReembolso(anyString()))
                .thenThrow(new ResourceNotFoundException("Pago no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos/pago-inexistente/reembolso")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(pagoService, times(1)).procesarReembolso("pago-inexistente");
    }

    @Test
    @DisplayName("POST /api/v1/pagos/{id}/reembolso - Pago no aprobado")
    @WithMockUser(username = "admin-123", roles = "ADMINISTRADOR")
    void procesarReembolso_PagoNoAprobado() throws Exception {
        // Arrange
        when(pagoService.procesarReembolso(anyString()))
                .thenThrow(new BusinessException("Solo se pueden reembolsar pagos aprobados"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos/pago-123/reembolso")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(pagoService, times(1)).procesarReembolso("pago-123");
    }

    @Test
    @DisplayName("POST /api/v1/pagos/{id}/reembolso - Error en pasarela")
    @WithMockUser(username = "admin-123", roles = "ADMINISTRADOR")
    void procesarReembolso_ErrorPasarela() throws Exception {
        // Arrange
        when(pagoService.procesarReembolso(anyString()))
                .thenThrow(new PaymentException("Error al procesar el reembolso"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/pagos/pago-123/reembolso")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isPaymentRequired());

        verify(pagoService, times(1)).procesarReembolso("pago-123");
    }


}