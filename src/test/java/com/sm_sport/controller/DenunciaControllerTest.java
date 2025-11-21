package com.sm_sport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm_sport.config.SecurityConfig;
import com.sm_sport.dto.request.CrearDenunciaRequest;

import com.sm_sport.dto.response.DenunciaResponse;
import com.sm_sport.model.enums.AccionDenuncia;
import com.sm_sport.model.enums.EstadoDenuncia;
import com.sm_sport.security.JwtAuthenticationFilter;
import com.sm_sport.service.DenunciaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.sm_sport.dto.request.ResponderDenunciaRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(DenunciaController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class DenunciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DenunciaService denunciaService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("123", null, "ROLE_CLIENTE")
        );
    }

    // ====================== CREAR DENUNCIA ======================
    @Test
    void crearDenuncia_ok() throws Exception {

        CrearDenunciaRequest request = CrearDenunciaRequest.builder()
                .idUsuarioDenunciado("userX")
                .tipoDenuncia("COMPORTAMIENTO_INAPROPIADO")
                .descripcion("El usuario tuvo un comportamiento inapropiado durante la reserva.")
                .evidencia("captura.png")
                .build();

        DenunciaResponse response = DenunciaResponse.builder()
                .idDenuncia("den1")
                .idUsuarioDenunciante("123")
                .idUsuarioDenunciado("userX")
                .descripcion(request.getDescripcion())
                .tipoDenuncia(request.getTipoDenuncia())
                .estado(EstadoDenuncia.PENDIENTE)
                .fechaDenuncia(LocalDateTime.now())
                .build();

        Mockito.when(denunciaService.crearDenuncia(eq("123"), any(CrearDenunciaRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/denuncias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDenuncia").value("den1"))
                .andExpect(jsonPath("$.idUsuarioDenunciante").value("123"));
    }

    // ====================== OBTENER POR ID ======================
    @Test
    void obtenerDenuncia_ok() throws Exception {

        DenunciaResponse response = DenunciaResponse.builder()
                .idDenuncia("D10")
                .idUsuarioDenunciante("123")
                .idUsuarioDenunciado("userX")
                .descripcion("Denuncia ejemplo")
                .estado(EstadoDenuncia.PENDIENTE)
                .build();

        Mockito.when(denunciaService.obtenerPorId("D10")).thenReturn(response);

        mockMvc.perform(get("/api/v1/denuncias/D10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDenuncia").value("D10"));
    }

    // ====================== LISTAR MIS DENUNCIAS ======================
    @Test
    void listarMisDenuncias_ok() throws Exception {

        DenunciaResponse d1 = DenunciaResponse.builder()
                .idDenuncia("DX")
                .idUsuarioDenunciante("123")
                .build();

        Mockito.when(denunciaService.listarPorDenunciante("123"))
                .thenReturn(List.of(d1));

        mockMvc.perform(get("/api/v1/denuncias/mis-denuncias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDenuncia").value("DX"));
    }

    // ====================== LISTAR DENUNCIAS CONTRA MI ======================
    @Test
    void listarDenunciasContraMi_ok() throws Exception {

        DenunciaResponse d1 = DenunciaResponse.builder()
                .idDenuncia("D90")
                .idUsuarioDenunciado("123")
                .build();

        Mockito.when(denunciaService.listarContraUsuario("123"))
                .thenReturn(List.of(d1));

        mockMvc.perform(get("/api/v1/denuncias/contra-mi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDenuncia").value("D90"));
    }

    // ====================== LISTAR PENDIENTES (ADMIN) ======================
    @Test
    void listarPendientes_ok() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin1", null, "ROLE_ADMINISTRADOR")
        );

        DenunciaResponse d1 = DenunciaResponse.builder()
                .idDenuncia("DP1")
                .estado(EstadoDenuncia.PENDIENTE)
                .build();

        Mockito.when(denunciaService.listarPendientes())
                .thenReturn(List.of(d1));

        mockMvc.perform(get("/api/v1/denuncias/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDenuncia").value("DP1"));
    }

    // ====================== RESPONDER DENUNCIA (ADMIN) ======================
    @Test
    void responderDenuncia_ok() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin1", null, "ROLE_ADMINISTRADOR")
        );

        ResponderDenunciaRequest request = new ResponderDenunciaRequest(
                "Se revisó la evidencia y se aplicó una advertencia.",
                AccionDenuncia.ADVERTENCIA
        );

        DenunciaResponse response = DenunciaResponse.builder()
                .idDenuncia("R10")
                .respuestaAdmin(request.getRespuesta())
                .accionTomada(request.getAccionTomada())
                .fechaRespuesta(LocalDateTime.now())
                .build();

        Mockito.when(denunciaService.responderDenuncia(eq("R10"), eq("admin1"), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/denuncias/R10/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuestaAdmin").value(request.getRespuesta()));
    }

    // ====================== DECLARAR IMPROCEDENTE (ADMIN) ======================
    @Test
    void declararImprocedente_ok() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin1", null, "ROLE_ADMINISTRADOR")
        );

        DenunciaResponse response = DenunciaResponse.builder()
                .idDenuncia("IMP1")
                .estado(EstadoDenuncia.IMPROCEDENTE)
                .build();

        Mockito.when(denunciaService.declararImprocedente("IMP1", "admin1"))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/denuncias/IMP1/improcedente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("IMPROCEDENTE"));
    }
}
