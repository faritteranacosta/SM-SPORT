package com.sm_sport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm_sport.dto.request.ActualizarEstadoUsuarioRequest;
import com.sm_sport.dto.request.ActualizarPerfilRequest;
import com.sm_sport.dto.request.FiltroUsuarioRequest;
import com.sm_sport.dto.response.*;
import com.sm_sport.model.enums.EstadoUsuario;
import com.sm_sport.model.enums.Rol;
import com.sm_sport.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para UsuarioController
 */
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        // limpiar contexto previo
        SecurityContextHolder.clearContext();
    }

    // ===================== obtenerUsuario (propio usuario) =====================
    @Test
    void obtenerUsuario_mismoUsuario_exito() throws Exception {
        // simular autenticación: usuario "user-1"
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-1", null, "ROLE_CLIENTE")
        );

        UsuarioResponse usuario = UsuarioResponse.builder()
                .idUsuario("user-1")
                .nombre("Usuario Uno")
                .correo("uno@mail.com")
                .build();

        when(usuarioService.obtenerPorId("user-1")).thenReturn(usuario);

        mockMvc.perform(get("/api/v1/usuarios/{id}", "user-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value("user-1"))
                .andExpect(jsonPath("$.correo").value("uno@mail.com"));

        verify(usuarioService, times(1)).obtenerPorId("user-1");
    }

    // ===================== obtenerUsuario (forbidden) =====================
    @Test
    void obtenerUsuario_otroUsuario_noAdmin_forbidden() throws Exception {
        // autenticado como user-1, intenta acceder a user-2
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-1", null, "ROLE_CLIENTE")
        );

        mockMvc.perform(get("/api/v1/usuarios/{id}", "user-2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).obtenerPorId(anyString());
    }

    // ===================== obtenerPerfil =====================
    @Test
    void obtenerPerfil_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-42", null, "ROLE_PROVEEDOR")
        );

        UsuarioResponse perfil = UsuarioResponse.builder()
                .idUsuario("user-42")
                .nombre("Proveedor Ejemplo")
                .correo("prov@mail.com")
                .build();

        when(usuarioService.obtenerPerfil("user-42")).thenReturn(perfil);

        mockMvc.perform(get("/api/v1/usuarios/perfil")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value("user-42"))
                .andExpect(jsonPath("$.nombre").value("Proveedor Ejemplo"));

        verify(usuarioService, times(1)).obtenerPerfil("user-42");
    }

    // ===================== actualizarPerfil =====================
    @Test
    void actualizarPerfil_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-77", null, "ROLE_CLIENTE")
        );

        ActualizarPerfilRequest request = new ActualizarPerfilRequest();
        request.setNombre("Nuevo Nombre");
        request.setTelefono("3001234567");
        request.setDireccion("Calle 45");

        UsuarioResponse updated = UsuarioResponse.builder()
                .idUsuario("user-77")
                .nombre("Nuevo Nombre")
                .telefono("3001234567")
                .direccion("Calle 45")
                .build();

        when(usuarioService.actualizarPerfil(eq("user-77"), any(ActualizarPerfilRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/usuarios/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value("user-77"))
                .andExpect(jsonPath("$.nombre").value("Nuevo Nombre"));

        verify(usuarioService, times(1)).actualizarPerfil(eq("user-77"), any(ActualizarPerfilRequest.class));
    }

    // ===================== eliminarUsuario (ADMIN) =====================
    @Test
    void eliminarUsuario_admin_exito() throws Exception {
        // autenticado como admin
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin-1", null, "ROLE_ADMINISTRADOR")
        );

        MessageResponse resp = MessageResponse.success("Usuario eliminado");

        when(usuarioService.eliminarUsuario("target-1")).thenReturn(resp);

        mockMvc.perform(delete("/api/v1/usuarios/{id}", "target-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario eliminado"));

        verify(usuarioService, times(1)).eliminarUsuario("target-1");
    }

    // ===================== obtenerCliente =====================
    @Test
    void obtenerCliente_exito() throws Exception {
        // proveedor o admin puede acceder — simulamos proveedor
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov-1", null, "ROLE_PROVEEDOR")
        );

        ClienteResponse cliente = ClienteResponse.builder()
                .idUsuario("cliente-9")
                .nombre("Cliente 9")
                .correo("cli9@mail.com")
                .preferenciaDeportes("Fútbol")
                .nivelExperiencia("Intermedio")
                .totalReservas(5)
                .resenasPublicadas(2)
                .build();

        when(usuarioService.obtenerCliente("cliente-9")).thenReturn(cliente);

        mockMvc.perform(get("/api/v1/usuarios/cliente/{idCliente}", "cliente-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value("cliente-9"))
                .andExpect(jsonPath("$.preferenciaDeportes").value("Fútbol"));

        verify(usuarioService, times(1)).obtenerCliente("cliente-9");
    }

    // ===================== obtenerProveedor =====================
    @Test
    void obtenerProveedor_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-x", null, "ROLE_CLIENTE")
        );

        ProveedorResponse proveedor = ProveedorResponse.builder()
                .idUsuario("prov-9")
                .nombre("Proveedor 9")
                .correo("prov9@mail.com")
                .saldoCuenta(new BigDecimal("150000.00"))
                .calificacionPromedio(new BigDecimal("4.5"))
                .totalServiciosPublicados(10)
                .verificado(Boolean.TRUE)
                .build();

        when(usuarioService.obtenerProveedor("prov-9")).thenReturn(proveedor);

        mockMvc.perform(get("/api/v1/usuarios/proveedor/{idProveedor}", "prov-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value("prov-9"))
                .andExpect(jsonPath("$.saldoCuenta").exists())
                .andExpect(jsonPath("$.verificado").value(true));

        verify(usuarioService, times(1)).obtenerProveedor("prov-9");
    }

    // ===================== buscarUsuarios (ADMIN) =====================
    @Test
    void buscarUsuarios_admin_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin-2", null, "ROLE_ADMINISTRADOR")
        );

        FiltroUsuarioRequest filtro = FiltroUsuarioRequest.builder()
                .nombre("Juan")
                .pagina(0)
                .tamano(10)
                .build();

        PageResponse<UsuarioResponse> page = PageResponse.<UsuarioResponse>builder()
                .content(List.of(UsuarioResponse.builder().idUsuario("u1").nombre("Juan").build()))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(usuarioService.listarUsuarios(any(FiltroUsuarioRequest.class))).thenReturn(page);

        mockMvc.perform(post("/api/v1/usuarios/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filtro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(usuarioService, times(1)).listarUsuarios(any(FiltroUsuarioRequest.class));
    }

    // ===================== cambiarEstado (ADMIN) =====================
    @Test
    void cambiarEstado_admin_exito() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin-3", null, "ROLE_ADMINISTRADOR")
        );

        ActualizarEstadoUsuarioRequest request = new ActualizarEstadoUsuarioRequest();
        request.setEstado(EstadoUsuario.INACTIVO);
        request.setMotivo("Incumplimiento");

        MessageResponse resp = MessageResponse.success("Estado actualizado");

        when(usuarioService.cambiarEstado(eq("target-99"), any(ActualizarEstadoUsuarioRequest.class)))
                .thenReturn(resp);

        mockMvc.perform(put("/api/v1/usuarios/{id}/estado", "target-99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Estado actualizado"));

        verify(usuarioService, times(1)).cambiarEstado(eq("target-99"), any(ActualizarEstadoUsuarioRequest.class));
    }
}
