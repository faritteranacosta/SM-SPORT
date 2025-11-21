package com.sm_sport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sm_sport.dto.request.*;
import com.sm_sport.dto.response.*;
import com.sm_sport.model.enums.EstadoServicio;
import com.sm_sport.service.ServicioService;
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
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Versión final corregida de tests para ServicioController
 */
class ServicioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServicioService servicioService;

    @InjectMocks
    private ServicioController servicioController;

    // ObjectMapper con soporte JSR310 (LocalDate / LocalTime)
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(servicioController).build();
        SecurityContextHolder.clearContext();
    }

    // ========================= PUBLICAR SERVICIO =========================

    @Test
    void publicarServicio_proveedor_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-1", null, "ROLE_PROVEEDOR")
        );

        UbicacionRequest ubicacion = UbicacionRequest.builder()
                .direccion("Calle 10 #5")
                .ciudad("Santa Marta")
                .departamento("Magdalena")
                .pais("Colombia")
                .coordenadasLat(BigDecimal.valueOf(11.004))
                .coordenadasLng(BigDecimal.valueOf(-74.807))
                .build();

        CrearServicioRequest req = CrearServicioRequest.builder()
                .nombre("Clases de fútbol")
                .deporte("Fútbol")
                .descripcion("Entrenamiento completo")
                .precio(new BigDecimal("50000"))
                .ubicacion(ubicacion)
                .build();

        ServicioResponse resp = ServicioResponse.builder()
                .idServicio("serv-1")
                .nombre("Clases de fútbol")
                .deporte("Fútbol")
                .precio(new BigDecimal("50000"))
                .idProveedor("prov-1")
                .estado(EstadoServicio.PUBLICADO)
                .build();

        when(servicioService.publicarServicio(eq("prov-1"), any(CrearServicioRequest.class)))
                .thenReturn(resp);

        mockMvc.perform(post("/api/v1/servicios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idServicio").value("serv-1"))
                .andExpect(jsonPath("$.nombre").value("Clases de fútbol"));

        verify(servicioService, times(1)).publicarServicio(eq("prov-1"), any(CrearServicioRequest.class));
    }

    // ========================= LISTAR SERVICIOS =========================

    @Test
    void listarServicios_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-x", null, "ROLE_CLIENTE")
        );

        PageResponse<ServicioResponse> page = PageResponse.<ServicioResponse>builder()
                .content(List.of(ServicioResponse.builder()
                        .idServicio("s1")
                        .nombre("Servicio 1")
                        .build()))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(servicioService.listarServicios(0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(servicioService, times(1)).listarServicios(0, 20);
    }

    // ========================= OBTENER SERVICIO =========================

    @Test
    void obtenerServicio_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cli-1", null, "ROLE_CLIENTE")
        );

        ServicioResponse resp = ServicioResponse.builder()
                .idServicio("serv-1")
                .nombre("Clases de fútbol")
                .build();

        when(servicioService.obtenerPorId("serv-1")).thenReturn(resp);

        mockMvc.perform(get("/api/v1/servicios/{id}", "serv-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idServicio").value("serv-1"));

        verify(servicioService, times(1)).obtenerPorId("serv-1");
    }

    // ========================= DETALLE =========================

    @Test
    void obtenerDetalleServicio_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cli-1", null, "ROLE_CLIENTE")
        );

        ServicioDetalleResponse detalle = ServicioDetalleResponse.builder()
                .idServicio("serv-88")
                .descripcion("Detalle completo")
                .build();

        when(servicioService.obtenerDetalle("serv-88")).thenReturn(detalle);

        mockMvc.perform(get("/api/v1/servicios/{id}/detalle", "serv-88"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idServicio").value("serv-88"));

        verify(servicioService, times(1)).obtenerDetalle("serv-88");
    }

    // ========================= ACTUALIZAR =========================

    @Test
    void actualizarServicio_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-1", null, "ROLE_PROVEEDOR")
        );

        ActualizarServicioRequest req = ActualizarServicioRequest.builder()
                .nombre("Nuevo nombre")
                .precio(new BigDecimal("60000"))
                .build();

        ServicioResponse resp = ServicioResponse.builder()
                .idServicio("serv-1")
                .nombre("Nuevo nombre")
                .build();

        when(servicioService.actualizarServicio(eq("serv-1"), any(ActualizarServicioRequest.class)))
                .thenReturn(resp);

        mockMvc.perform(put("/api/v1/servicios/{id}", "serv-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo nombre"));

        verify(servicioService, times(1)).actualizarServicio(eq("serv-1"), any());
    }

    // ========================= ELIMINAR =========================

    @Test
    void eliminarServicio_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-1", null, "ROLE_PROVEEDOR")
        );

        MessageResponse resp = MessageResponse.success("Eliminado");

        when(servicioService.eliminarServicio("serv-2")).thenReturn(resp);

        mockMvc.perform(delete("/api/v1/servicios/{id}", "serv-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Eliminado"));

        verify(servicioService, times(1)).eliminarServicio("serv-2");
    }

    // ========================= BUSCAR =========================

    @Test
    void buscarServicios_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente", null, "ROLE_CLIENTE")
        );

        BusquedaServicioRequest req = BusquedaServicioRequest.builder()
                .ciudad("Santa Marta")
                .pagina(0)
                .tamano(10)
                .build();

        PageResponse<ServicioResponse> page = PageResponse.<ServicioResponse>builder()
                .content(List.of(ServicioResponse.builder().idServicio("ss1").build()))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(servicioService.buscarServicios(any(BusquedaServicioRequest.class))).thenReturn(page);

        mockMvc.perform(post("/api/v1/servicios/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(servicioService, times(1)).buscarServicios(any(BusquedaServicioRequest.class));
    }

    // ========================= CERCANOS =========================

    @Test
    void buscarServiciosCercanos_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("cliente", null, "ROLE_CLIENTE")
        );

        List<ServicioResponse> servicios = List.of(
                ServicioResponse.builder().idServicio("c1").build()
        );

        when(servicioService.buscarServiciosCercanos(11.0, -74.8, 5))
                .thenReturn(servicios);

        mockMvc.perform(get("/api/v1/servicios/cercanos")
                        .param("latitud", "11.0")
                        .param("longitud", "-74.8")
                        .param("radioKm", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idServicio").value("c1"));

        verify(servicioService, times(1)).buscarServiciosCercanos(11.0, -74.8, 5);
    }

    // ========================= SERVICIOS POR PROVEEDOR =========================

    @Test
    void listarServiciosProveedor_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", null, "ROLE_ADMINISTRADOR")
        );

        List<ServicioResponse> servicios = List.of(
                ServicioResponse.builder().idServicio("s1").build()
        );

        when(servicioService.listarPorProveedor("PROV99"))
                .thenReturn(servicios);

        mockMvc.perform(get("/api/v1/servicios/proveedor/{idProveedor}", "PROV99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idServicio").value("s1"));

        verify(servicioService, times(1)).listarPorProveedor("PROV99");
    }

    // ========================= MIS SERVICIOS =========================

    @Test
    void listarMisServicios_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-1", null, "ROLE_PROVEEDOR")
        );

        List<ServicioResponse> servicios = List.of(
                ServicioResponse.builder().idServicio("own-1").build()
        );

        when(servicioService.listarPorProveedor("prov-1"))
                .thenReturn(servicios);

        mockMvc.perform(get("/api/v1/servicios/mis-servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idServicio").value("own-1"));

        verify(servicioService, times(1)).listarPorProveedor("prov-1");
    }

    // ========================= AGREGAR DISPONIBILIDAD =========================

    @Test
    void agregarDisponibilidad_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-1", null, "ROLE_PROVEEDOR")
        );

        DisponibilidadRequest disponibilidadItem = DisponibilidadRequest.builder()
                .fecha(LocalDate.now().plusDays(1))
                .horaInicio(LocalTime.parse("08:00"))
                .horaFin(LocalTime.parse("10:00"))
                .cuposDisponibles(5)
                .build();

        List<DisponibilidadRequest> disponibilidad = List.of(disponibilidadItem);

        MessageResponse resp = MessageResponse.success("Disponibilidad agregada");

        when(servicioService.agregarDisponibilidad(eq("serv-90"), anyList()))
                .thenReturn(resp);

        mockMvc.perform(post("/api/v1/servicios/{id}/disponibilidad", "serv-90")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disponibilidad)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Disponibilidad agregada"));

        verify(servicioService, times(1)).agregarDisponibilidad(eq("serv-90"), anyList());
    }

    // ========================= CAMBIAR ESTADO =========================

    @Test
    void cambiarEstado_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-1", null, "ROLE_PROVEEDOR")
        );

        ServicioResponse resp = ServicioResponse.builder()
                .idServicio("serv-1")
                .estado(EstadoServicio.PAUSADO)
                .build();

        when(servicioService.cambiarEstado("serv-1", EstadoServicio.PAUSADO))
                .thenReturn(resp);

        mockMvc.perform(put("/api/v1/servicios/{id}/estado", "serv-1")
                        .param("estado", "PAUSADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAUSADO"));

        verify(servicioService, times(1)).cambiarEstado("serv-1", EstadoServicio.PAUSADO);
    }
}
