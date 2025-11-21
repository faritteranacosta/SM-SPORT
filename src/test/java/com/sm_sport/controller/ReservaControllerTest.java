package com.sm_sport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sm_sport.dto.request.CancelarReservaRequest;
import com.sm_sport.dto.request.CrearReservaRequest;
import com.sm_sport.dto.request.FiltroReservaRequest;
import com.sm_sport.dto.response.*;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReservaService reservaService;

    @InjectMocks
    private ReservaController reservaController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reservaController).build();
        SecurityContextHolder.clearContext();
    }

    // ========================= CREAR RESERVA =========================
    @Test
    void crearReserva_cliente_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente-1", null, "ROLE_CLIENTE")
        );

        CrearReservaRequest req = CrearReservaRequest.builder()
                .idServicio("serv-10")
                .fechaReserva(LocalDate.now().plusDays(2))
                .horaReserva(LocalTime.of(10, 0))
                .notasCliente("Quiero clase privada")
                .build();

        ReservaResponse resp = ReservaResponse.builder()
                .idReserva("res-100")
                .idCliente("cliente-1")
                .idServicio("serv-10")
                .fechaReserva(req.getFechaReserva())
                .horaReserva(req.getHoraReserva())
                .estado(EstadoReserva.PENDIENTE)
                .costoTotal(new BigDecimal("50000"))
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(reservaService.crearReserva(eq("cliente-1"), any(CrearReservaRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idReserva").value("res-100"))
                .andExpect(jsonPath("$.idCliente").value("cliente-1"));

        verify(reservaService, times(1)).crearReserva(eq("cliente-1"), any(CrearReservaRequest.class));
    }

    // ========================= LISTAR MIS RESERVAS (CLIENTE) =========================
    @Test
    void listarMisReservas_cliente_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente-2", null, "ROLE_CLIENTE")
        );

        PageResponse<ReservaResponse> page = PageResponse.<ReservaResponse>builder()
                .content(List.of(ReservaResponse.builder()
                        .idReserva("r1")
                        .idCliente("cliente-2")
                        .build()))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(reservaService.listarPorCliente("cliente-2", 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/reservas")
                        .param("pagina", "0")
                        .param("tamano", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(reservaService, times(1)).listarPorCliente("cliente-2", 0, 20);
    }

    // ========================= LISTAR MIS RESERVAS (PROVEEDOR) =========================
    @Test
    void listarMisReservas_proveedor_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-5", null, "ROLE_PROVEEDOR")
        );

        PageResponse<ReservaResponse> page = PageResponse.<ReservaResponse>builder()
                .content(List.of(ReservaResponse.builder()
                        .idReserva("r2")
                        .idProveedor("prov-5")
                        .build()))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(reservaService.listarPorProveedor("prov-5", 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/reservas")
                        .param("pagina", "0")
                        .param("tamano", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(reservaService, times(1)).listarPorProveedor("prov-5", 0, 20);
    }

    // ========================= OBTENER RESERVA (AUTORIZACION OK) =========================
    @Test
    void obtenerReserva_autorizado_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente-3", null, "ROLE_CLIENTE")
        );

        ReservaResponse resp = ReservaResponse.builder()
                .idReserva("res-200")
                .idCliente("cliente-3")
                .idProveedor("prov-7")
                .idServicio("serv-200")
                .build();

        when(reservaService.obtenerPorId("res-200")).thenReturn(resp);

        mockMvc.perform(get("/api/v1/reservas/{id}", "res-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReserva").value("res-200"));

        verify(reservaService, times(1)).obtenerPorId("res-200");
    }

    // ========================= OBTENER RESERVA (FORBIDDEN) =========================
    @Test
    void obtenerReserva_noPertenece_forbidden() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente-X", null, "ROLE_CLIENTE")
        );

        ReservaResponse resp = ReservaResponse.builder()
                .idReserva("res-201")
                .idCliente("otro-cliente")
                .idProveedor("prov-9")
                .build();

        when(reservaService.obtenerPorId("res-201")).thenReturn(resp);

        mockMvc.perform(get("/api/v1/reservas/{id}", "res-201"))
                .andExpect(status().isForbidden());

        verify(reservaService, times(1)).obtenerPorId("res-201");
    }

    // ========================= OBTENER DETALLE (AUTORIZACION) =========================
    @Test
    void obtenerDetalleReserva_autorizado_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente-4", null, "ROLE_CLIENTE")
        );

        ClienteResponse cliente = ClienteResponse.builder()
                .idUsuario("cliente-4")
                .nombre("Cliente 4")
                .build();

        ProveedorResponse proveedor = ProveedorResponse.builder()
                .idUsuario("prov-4")
                .nombre("Proveedor 4")
                .correo("p4@example.com")
                .build();

        ServicioDetalleResponse servicio = ServicioDetalleResponse.builder()
                .idServicio("serv-300")
                .nombre("Servicio 300")
                .proveedor(proveedor) // ← FIX AQUÍ
                .build();

        ReservaDetalleResponse detalle = ReservaDetalleResponse.builder()
                .idReserva("res-300")
                .cliente(cliente)
                .servicio(servicio)
                .build();

        when(reservaService.obtenerDetalle("res-300")).thenReturn(detalle);

        mockMvc.perform(get("/api/v1/reservas/{id}/detalle", "res-300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReserva").value("res-300"));

        verify(reservaService, times(1)).obtenerDetalle("res-300");
    }


    // ========================= CONFIRMAR RESERVA (PROVEEDOR) =========================
    @Test
    void confirmarReserva_proveedor_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-10", null, "ROLE_PROVEEDOR")
        );

        ReservaResponse resp = ReservaResponse.builder()
                .idReserva("res-400")
                .estado(EstadoReserva.CONFIRMADA)
                .build();

        when(reservaService.confirmarReserva("res-400", "prov-10")).thenReturn(resp);

        mockMvc.perform(post("/api/v1/reservas/{id}/confirmar", "res-400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        verify(reservaService, times(1)).confirmarReserva("res-400", "prov-10");
    }

    // ========================= RECHAZAR RESERVA (PROVEEDOR) =========================
    @Test
    void rechazarReserva_proveedor_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-11", null, "ROLE_PROVEEDOR")
        );

        ReservaResponse resp = ReservaResponse.builder()
                .idReserva("res-401")
                .estado(EstadoReserva.RECHAZADA)
                .build();

        when(reservaService.rechazarReserva("res-401", "prov-11", "Motivo válido")).thenReturn(resp);

        mockMvc.perform(post("/api/v1/reservas/{id}/rechazar", "res-401")
                        .param("motivo", "Motivo válido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));

        verify(reservaService, times(1)).rechazarReserva("res-401", "prov-11", "Motivo válido");
    }

    // ========================= CANCELAR RESERVA (CLIENTE) =========================
    @Test
    void cancelarReserva_cliente_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente-5", null, "ROLE_CLIENTE")
        );

        CancelarReservaRequest req = new CancelarReservaRequest();
        req.setMotivoCancelacion("Motivo suficiente para cancelar");
        req.setSolicitarReembolso(true);

        MessageResponse resp = MessageResponse.success("Reserva cancelada");

        when(reservaService.cancelarReserva("res-500", "cliente-5", req)).thenReturn(resp);

        mockMvc.perform(delete("/api/v1/reservas/{id}", "res-500")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reserva cancelada"));

        verify(reservaService, times(1)).cancelarReserva("res-500", "cliente-5", req);
    }

    // ========================= FINALIZAR RESERVA (PROVEEDOR) =========================
    @Test
    void finalizarReserva_proveedor_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-12", null, "ROLE_PROVEEDOR")
        );

        ReservaResponse resp = ReservaResponse.builder()
                .idReserva("res-600")
                .estado(EstadoReserva.FINALIZADA)
                .build();

        when(reservaService.finalizarReserva("res-600")).thenReturn(resp);

        mockMvc.perform(post("/api/v1/reservas/{id}/finalizar", "res-600"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"));
        ;

        verify(reservaService, times(1)).finalizarReserva("res-600");
    }

    // ========================= BUSCAR RESERVAS (PROVEEDOR/ADMIN) =========================
    @Test
    void buscarReservas_proveedor_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-20", null, "ROLE_PROVEEDOR")
        );

        FiltroReservaRequest filtro = FiltroReservaRequest.builder()
                .idProveedor("prov-20")
                .pagina(0)
                .tamano(10)
                .build();

        PageResponse<ReservaResponse> page = PageResponse.<ReservaResponse>builder()
                .content(List.of(ReservaResponse.builder().idReserva("rX").build()))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(reservaService.filtrarReservas(any(FiltroReservaRequest.class))).thenReturn(page);

        mockMvc.perform(post("/api/v1/reservas/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filtro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(reservaService, times(1)).filtrarReservas(any(FiltroReservaRequest.class));
    }

    // ========================= VERIFICAR DISPONIBILIDAD =========================
    @Test
    void verificarDisponibilidad_cliente_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente-7", null, "ROLE_CLIENTE")
        );

        CrearReservaRequest req = CrearReservaRequest.builder()
                .idServicio("serv-99")
                .fechaReserva(LocalDate.now().plusDays(3))
                .horaReserva(LocalTime.of(14, 0))
                .build();

        when(reservaService.verificarDisponibilidad(eq("serv-99"), any(CrearReservaRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/reservas/verificar-disponibilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        verify(reservaService, times(1)).verificarDisponibilidad(eq("serv-99"), any(CrearReservaRequest.class));
    }
}
