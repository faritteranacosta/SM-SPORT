package com.sm_sport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.NotificacionResponse;
import com.sm_sport.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionController notificacionController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(notificacionController).build();

        // Configurar usuario autenticado falso
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        TestingAuthenticationToken auth =
                new TestingAuthenticationToken("user123", null, "ROLE_CLIENTE");
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    // ============================================================
    // TEST: LISTAR TODAS LAS NOTIFICACIONES
    // ============================================================
    @Test
    void testListarNotificaciones() throws Exception {

        NotificacionResponse notif = NotificacionResponse.builder()
                .idNotificacion("notif1")
                .tipoNotificacion("SISTEMA")
                .titulo("Bienvenido")
                .mensaje("Mensaje de prueba")
                .leida(false)
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(notificacionService.listarPorUsuario("user123"))
                .thenReturn(List.of(notif));

        mockMvc.perform(get("/api/v1/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idNotificacion").value("notif1"));
    }

    // ============================================================
    // TEST: LISTAR NO LEÍDAS
    // ============================================================
    @Test
    void testListarNoLeidas() throws Exception {

        NotificacionResponse notif = NotificacionResponse.builder()
                .idNotificacion("notif2")
                .tipoNotificacion("RESERVA")
                .titulo("Reserva pendiente")
                .mensaje("Tienes una nueva reserva")
                .leida(false)
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(notificacionService.listarNoLeidas("user123"))
                .thenReturn(List.of(notif));

        mockMvc.perform(get("/api/v1/notificaciones/no-leidas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idNotificacion").value("notif2"));
    }

    // ============================================================
    // TEST: MARCAR COMO LEÍDA
    // ============================================================
    @Test
    void testMarcarComoLeida() throws Exception {

        MessageResponse response = MessageResponse.success("Notificación marcada");

        when(notificacionService.marcarComoLeida("notif3", "user123"))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/notificaciones/notif3/leer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notificación marcada"));
    }

    // ============================================================
    // TEST: MARCAR TODAS COMO LEÍDAS
    // ============================================================
    @Test
    void testMarcarTodasLeidas() throws Exception {

        MessageResponse response = MessageResponse.success("Todas marcadas");

        when(notificacionService.marcarTodasComoLeidas("user123"))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/notificaciones/marcar-todas-leidas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todas marcadas"));
    }

    // ============================================================
    // TEST: ENVIAR NOTIFICACIÓN TEST (ADMIN)
    // ============================================================
    @Test
    void testEnviarNotificacionTest() throws Exception {

        // Cambiar rol momentáneamente a ADMINISTRADOR
        TestingAuthenticationToken auth =
                new TestingAuthenticationToken("admin123", null, "ROLE_ADMINISTRADOR");
        SecurityContextHolder.getContext().setAuthentication(auth);

        doNothing().when(notificacionService)
                .enviarNotificacion(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/v1/notificaciones/test/enviar")
                        .param("tipo", "SISTEMA")
                        .param("titulo", "Prueba")
                        .param("mensaje", "Mensaje de prueba admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Notificación de prueba enviada"));
    }
}
