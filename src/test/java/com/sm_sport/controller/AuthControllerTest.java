package com.sm_sport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm_sport.dto.request.*;
import com.sm_sport.dto.response.AuthResponse;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.model.enums.Rol;
import com.sm_sport.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    // ===========================================================
    //                 TEST: REGISTRO
    // ===========================================================

    @Test
    void registrarUsuario_exito() throws Exception {

        RegistroUsuarioRequest request = RegistroUsuarioRequest.builder()
                .nombre("Carlos Díaz")
                .correo("carlos@gmail.com")
                .contrasena("Password#1")
                .telefono("3001234567")
                .direccion("Calle 123")
                .rol(Rol.CLIENTE)
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-123")
                .refreshToken("refresh-token")
                .idUsuario("user-123")
                .correo("carlos@gmail.com")
                .rol(Rol.CLIENTE)
                .build();

        when(authService.registrar(any(RegistroUsuarioRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.correo").value("carlos@gmail.com"));

        verify(authService, times(1)).registrar(any());
    }

    // ===========================================================
    //                     TEST: LOGIN
    // ===========================================================

    @Test
    void login_exito() throws Exception {

        LoginRequest request = LoginRequest.builder()
                .correo("test@gmail.com")
                .contrasena("Password#1")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .correo("test@gmail.com")
                .rol(Rol.CLIENTE)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.correo").value("test@gmail.com"));

        verify(authService, times(1)).login(any());
    }

    // ===========================================================
    //                    TEST: LOGOUT
    // ===========================================================

    @Test
    void logout_exito() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("correo@correo.com", null)
        );

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada exitosamente"));

    }

    // ===========================================================
    //               TEST: CAMBIAR CONTRASEÑA
    // ===========================================================

    @Test
    void cambiarContrasena_exito() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-123", null)
        );

        CambiarContrasenaRequest request = new CambiarContrasenaRequest(
                "OldPass#1",
                "NewPass#22",
                "NewPass#22"
        );

        MessageResponse response = MessageResponse.success("Contraseña cambiada exitosamente");

        when(authService.cambiarContrasena(eq("user-123"), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/auth/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña cambiada exitosamente"));

        verify(authService, times(1)).cambiarContrasena(eq("user-123"), any());
    }

    // ===========================================================
    //               TEST: RECUPERAR CONTRASEÑA
    // ===========================================================

    @Test
    void recuperarContrasena_exito() throws Exception {

        RecuperarContrasenaRequest request = new RecuperarContrasenaRequest("test@gmail.com");

        MessageResponse response = MessageResponse.success("Correo enviado");

        when(authService.recuperarContrasena(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/recuperar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(authService, times(1)).recuperarContrasena(any());
    }

    // ===========================================================
    //               TEST: VALIDAR TOKEN
    // ===========================================================

    @Test
    void validarToken_exito() throws Exception {

        when(authService.validarToken("abc123")).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/validar-token")
                        .param("token", "abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        verify(authService, times(1)).validarToken("abc123");
    }

    // ===========================================================
    //               TEST: REFRESCAR TOKEN
    // ===========================================================

    @Test
    void refrescarToken_exito() throws Exception {

        AuthResponse response = AuthResponse.builder()
                .token("nuevo-token")
                .refreshToken("nuevo-refresh")
                .build();

        when(authService.refrescarToken("refresh123")).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .param("refreshToken", "refresh123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("nuevo-token"));

        verify(authService, times(1)).refrescarToken("refresh123");
    }
}
