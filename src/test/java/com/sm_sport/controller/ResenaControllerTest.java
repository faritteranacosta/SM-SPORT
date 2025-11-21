package com.sm_sport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm_sport.dto.request.CrearResenaRequest;
import com.sm_sport.dto.request.ResponderResenaRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.ResenaResponse;
import com.sm_sport.model.enums.EstadoRevision;
import com.sm_sport.security.JwtAuthenticationFilter;
import com.sm_sport.service.ResenaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(ResenaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResenaService resenaService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setupSecurity() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("123", null, "ROLE_CLIENTE")
        );
    }

    // --------------------------------------------------------
    // TEST: Crear reseña
    // --------------------------------------------------------
    @Test
    void crearResena_ok() throws Exception {

        CrearResenaRequest request = CrearResenaRequest.builder()
                .idReserva("res123")
                .calificacion(5)
                .comentario("Excelente servicio, muy recomendado.")
                .build();

        ResenaResponse response = ResenaResponse.builder()
                .idResena("abc123")
                .calificacion(5)
                .comentario("Excelente servicio, muy recomendado.")
                .fechaCreacion(LocalDateTime.now())
                .idCliente("123")
                .idServicio("serv1")
                .estadoRevision(EstadoRevision.PUBLICADA)
                .build();

        Mockito.when(resenaService.crearResena(eq("123"), any(CrearResenaRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/resenas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idResena").value("abc123"))
                .andExpect(jsonPath("$.calificacion").value(5));
    }

    // --------------------------------------------------------
    // TEST: Obtener reseña por ID
    // --------------------------------------------------------
    @Test
    void obtenerResena_ok() throws Exception {

        ResenaResponse response = ResenaResponse.builder()
                .idResena("resX")
                .calificacion(4)
                .comentario("Muy buen servicio")
                .fechaCreacion(LocalDateTime.now())
                .idCliente("123")
                .idServicio("serv1")
                .estadoRevision(EstadoRevision.PUBLICADA)
                .build();

        Mockito.when(resenaService.obtenerPorId("resX")).thenReturn(response);

        mockMvc.perform(get("/api/v1/resenas/resX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idResena").value("resX"))
                .andExpect(jsonPath("$.calificacion").value(4));
    }

    // --------------------------------------------------------
    // TEST: Listar reseñas por servicio (paginado)
    // --------------------------------------------------------
    @Test
    void listarResenasPorServicio_ok() throws Exception {

        ResenaResponse r1 = ResenaResponse.builder()
                .idResena("r1")
                .calificacion(5)
                .comentario("Perfecto")
                .build();

        Page<ResenaResponse> page = new PageImpl<>(
                List.of(r1),
                PageRequest.of(0, 10),
                1
        );

        Mockito.when(resenaService.listarPorServicio(eq("serv1"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/resenas/servicio/serv1")
                        .param("pagina", "0")
                        .param("tamano", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idResena").value("r1"));
    }

    // --------------------------------------------------------
    // TEST: Listar reseñas del cliente autenticado
    // --------------------------------------------------------
    @Test
    void listarMisResenas_ok() throws Exception {

        ResenaResponse r1 = ResenaResponse.builder()
                .idResena("z1")
                .calificacion(5)
                .comentario("Genial")
                .build();

        Mockito.when(resenaService.listarPorCliente("123"))
                .thenReturn(List.of(r1));

        mockMvc.perform(get("/api/v1/resenas/mis-resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idResena").value("z1"));
    }

    // --------------------------------------------------------
    // TEST: Responder reseña (proveedor)
    // --------------------------------------------------------
    @Test
    void responderResena_ok() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("prov123", null, "ROLE_PROVEEDOR")
        );

        ResponderResenaRequest request = new ResponderResenaRequest(
                "Muchas gracias por su comentario positivo."
        );

        ResenaResponse response = ResenaResponse.builder()
                .idResena("r10")
                .respuestaProveedor("Muchas gracias por su comentario positivo.")
                .fechaRespuesta(LocalDateTime.now())
                .build();

        Mockito.when(resenaService.responderResena(eq("r10"), eq("prov123"), any(ResponderResenaRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/resenas/r10/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuestaProveedor").value("Muchas gracias por su comentario positivo."));
    }

    // --------------------------------------------------------
    // TEST: Reportar reseña
    // --------------------------------------------------------
    @Test
    void reportarResena_ok() throws Exception {

        MessageResponse msg = MessageResponse.success("Reseña reportada");

        Mockito.when(resenaService.reportarResena(eq("r50"), eq("123")))
                .thenReturn(msg);

        mockMvc.perform(post("/api/v1/resenas/r50/reportar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reseña reportada"));
    }

    // --------------------------------------------------------
    // TEST: Eliminar reseña
    // --------------------------------------------------------
    @Test
    void eliminarResena_ok() throws Exception {

        MessageResponse msg = MessageResponse.success("Reseña eliminada");

        Mockito.when(resenaService.eliminarResena("r20", "123"))
                .thenReturn(msg);

        mockMvc.perform(delete("/api/v1/resenas/r20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reseña eliminada"));
    }
}
