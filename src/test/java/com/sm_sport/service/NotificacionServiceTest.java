package com.sm_sport.service;

import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.NotificacionResponse;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.NotificacionMapper;
import com.sm_sport.model.entity.Cliente;
import com.sm_sport.model.entity.Notificacion;
import com.sm_sport.model.entity.Usuario;
import com.sm_sport.repository.NotificacionRepository;
import com.sm_sport.repository.UsuarioRepository;
import com.sm_sport.service.impl.NotificacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para NotificacionServiceImpl")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private NotificacionMapper notificacionMapper;

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    // Objetos de prueba reutilizables
    private Usuario usuario;
    private Notificacion notificacion;
    private NotificacionResponse notificacionResponse;

    @BeforeEach
    void setUp() {
        // Configurar Usuario
        usuario = new Cliente();
        usuario.setIdUsuario("user-001");
        usuario.setNombre("Juan Usuario");
        usuario.setCorreo("juan@test.com");

        // Configurar Notificación
        notificacion = Notificacion.builder()
                .idNotificacion("notif-001")
                .usuario(usuario)
                .tipoNotificacion("RESERVA")
                .titulo("Nueva Reserva")
                .mensaje("Has recibido una nueva reserva")
                .leida(false)
                .fechaEnvio(LocalDateTime.now())
                .build();

        // Configurar Response
        notificacionResponse = NotificacionResponse.builder()
                .idNotificacion("notif-001")
                .tipoNotificacion("RESERVA")
                .titulo("Nueva Reserva")
                .mensaje("Has recibido una nueva reserva")
                .leida(false)
                .fechaEnvio(LocalDateTime.now())
                .build();
    }

    // ==================== TESTS ENVIAR NOTIFICACION ====================
    @Nested
    @DisplayName("Tests para enviarNotificacion()")
    class EnviarNotificacionTests {

        @Test
        @DisplayName("Debe enviar notificación exitosamente cuando usuario existe")
        void enviarNotificacion_UsuarioExiste_EnviaExitosamente() {
            // Arrange
            when(usuarioRepository.findById("user-001")).thenReturn(Optional.of(usuario));
            when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacion);

            // Act
            notificacionService.enviarNotificacion(
                    "user-001", "RESERVA", "Nueva Reserva", "Has recibido una nueva reserva"
            );

            // Assert
            verify(usuarioRepository).findById("user-001");
            verify(notificacionRepository).save(argThat(n ->
                    n.getTipoNotificacion().equals("RESERVA") &&
                            n.getTitulo().equals("Nueva Reserva") &&
                            n.getLeida().equals(false)
            ));
        }

        @Test
        @DisplayName("No debe lanzar excepción cuando usuario no existe (maneja error internamente)")
        void enviarNotificacion_UsuarioNoExiste_NoLanzaExcepcion() {
            // Arrange
            when(usuarioRepository.findById("user-999")).thenReturn(Optional.empty());

            // Act & Assert - No debe lanzar excepción
            assertThatCode(() -> notificacionService.enviarNotificacion(
                    "user-999", "RESERVA", "Título", "Mensaje"
            )).doesNotThrowAnyException();

            verify(notificacionRepository, never()).save(any());
        }

        @Test
        @DisplayName("No debe lanzar excepción cuando ocurre error al guardar")
        void enviarNotificacion_ErrorAlGuardar_NoLanzaExcepcion() {
            // Arrange
            when(usuarioRepository.findById("user-001")).thenReturn(Optional.of(usuario));
            when(notificacionRepository.save(any(Notificacion.class)))
                    .thenThrow(new RuntimeException("Error de BD"));

            // Act & Assert - No debe lanzar excepción
            assertThatCode(() -> notificacionService.enviarNotificacion(
                    "user-001", "RESERVA", "Título", "Mensaje"
            )).doesNotThrowAnyException();
        }
    }

    // ==================== TESTS LISTAR POR USUARIO ====================
    @Nested
    @DisplayName("Tests para listarPorUsuario()")
    class ListarPorUsuarioTests {

        @Test
        @DisplayName("Debe retornar lista de notificaciones cuando usuario existe")
        void listarPorUsuario_UsuarioExiste_RetornaLista() {
            // Arrange
            List<Notificacion> notificaciones = List.of(notificacion);
            List<NotificacionResponse> responses = List.of(notificacionResponse);

            when(usuarioRepository.existsById("user-001")).thenReturn(true);
            when(notificacionRepository.findByUsuarioIdUsuario("user-001"))
                    .thenReturn(notificaciones);
            when(notificacionMapper.toResponseList(notificaciones)).thenReturn(responses);

            // Act
            List<NotificacionResponse> resultado = notificacionService.listarPorUsuario("user-001");

            // Assert
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getIdNotificacion()).isEqualTo("notif-001");
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando usuario no tiene notificaciones")
        void listarPorUsuario_SinNotificaciones_RetornaListaVacia() {
            // Arrange
            when(usuarioRepository.existsById("user-001")).thenReturn(true);
            when(notificacionRepository.findByUsuarioIdUsuario("user-001"))
                    .thenReturn(Collections.emptyList());
            when(notificacionMapper.toResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            // Act
            List<NotificacionResponse> resultado = notificacionService.listarPorUsuario("user-001");

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando usuario no existe")
        void listarPorUsuario_UsuarioNoExiste_LanzaExcepcion() {
            // Arrange
            when(usuarioRepository.existsById("user-999")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> notificacionService.listarPorUsuario("user-999"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");

            verify(notificacionRepository, never()).findByUsuarioIdUsuario(any());
        }
    }

    // ==================== TESTS LISTAR NO LEIDAS ====================
    @Nested
    @DisplayName("Tests para listarNoLeidas()")
    class ListarNoLeidasTests {

        @Test
        @DisplayName("Debe retornar notificaciones no leídas cuando existen")
        void listarNoLeidas_ConNotificaciones_RetornaLista() {
            // Arrange
            List<Notificacion> notificaciones = List.of(notificacion);
            List<NotificacionResponse> responses = List.of(notificacionResponse);

            when(usuarioRepository.existsById("user-001")).thenReturn(true);
            when(notificacionRepository.findNotificacionesNoLeidas("user-001"))
                    .thenReturn(notificaciones);
            when(notificacionMapper.toResponseList(notificaciones)).thenReturn(responses);

            // Act
            List<NotificacionResponse> resultado = notificacionService.listarNoLeidas("user-001");

            // Assert
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getLeida()).isFalse();
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay notificaciones no leídas")
        void listarNoLeidas_TodasLeidas_RetornaListaVacia() {
            // Arrange
            when(usuarioRepository.existsById("user-001")).thenReturn(true);
            when(notificacionRepository.findNotificacionesNoLeidas("user-001"))
                    .thenReturn(Collections.emptyList());
            when(notificacionMapper.toResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            // Act
            List<NotificacionResponse> resultado = notificacionService.listarNoLeidas("user-001");

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando usuario no existe")
        void listarNoLeidas_UsuarioNoExiste_LanzaExcepcion() {
            // Arrange
            when(usuarioRepository.existsById("user-999")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> notificacionService.listarNoLeidas("user-999"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");
        }
    }

    // ==================== TESTS MARCAR COMO LEIDA ====================
    @Nested
    @DisplayName("Tests para marcarComoLeida()")
    class MarcarComoLeidaTests {

        @Test
        @DisplayName("Debe marcar notificación como leída exitosamente")
        void marcarComoLeida_NotificacionExiste_MarcaExitosamente() {
            // Arrange
            when(notificacionRepository.findById("notif-001")).thenReturn(Optional.of(notificacion));
            when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacion);

            // Act
            MessageResponse resultado = notificacionService.marcarComoLeida("notif-001", "user-001");

            // Assert
            assertThat(resultado.getSuccess()).isTrue();
            assertThat(resultado.getMessage()).isEqualTo("Notificación marcada como leída");
            verify(notificacionRepository).save(argThat(n ->
                    n.getLeida().equals(true) && n.getFechaLectura() != null
            ));
        }

        @Test
        @DisplayName("Debe retornar mensaje cuando notificación ya está leída")
        void marcarComoLeida_YaLeida_RetornaMensaje() {
            // Arrange
            notificacion.setLeida(true);
            notificacion.setFechaLectura(LocalDateTime.now());
            when(notificacionRepository.findById("notif-001")).thenReturn(Optional.of(notificacion));

            // Act
            MessageResponse resultado = notificacionService.marcarComoLeida("notif-001", "user-001");

            // Assert
            assertThat(resultado.getSuccess()).isTrue();
            assertThat(resultado.getMessage()).isEqualTo("La notificación ya estaba marcada como leída");
            verify(notificacionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando notificación no existe")
        void marcarComoLeida_NotificacionNoExiste_LanzaExcepcion() {
            // Arrange
            when(notificacionRepository.findById("notif-999")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> notificacionService.marcarComoLeida("notif-999", "user-001"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Notificación no encontrada");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando notificación no pertenece al usuario")
        void marcarComoLeida_NotificacionDeOtroUsuario_LanzaExcepcion() {
            // Arrange
            when(notificacionRepository.findById("notif-001")).thenReturn(Optional.of(notificacion));

            // Act & Assert
            assertThatThrownBy(() -> notificacionService.marcarComoLeida("notif-001", "otro-usuario"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("no pertenece al usuario");

            verify(notificacionRepository, never()).save(any());
        }
    }

    // ==================== TESTS MARCAR TODAS COMO LEIDAS ====================
    @Nested
    @DisplayName("Tests para marcarTodasComoLeidas()")
    class MarcarTodasComoLeidasTests {

        @Test
        @DisplayName("Debe marcar todas las notificaciones como leídas")
        void marcarTodasComoLeidas_ConNotificaciones_MarcaTodas() {
            // Arrange
            when(usuarioRepository.existsById("user-001")).thenReturn(true);
            when(notificacionRepository.contarNotificacionesNoLeidas("user-001")).thenReturn(5L);

            // Act
            MessageResponse resultado = notificacionService.marcarTodasComoLeidas("user-001");

            // Assert
            assertThat(resultado.getSuccess()).isTrue();
            assertThat(resultado.getMessage()).contains("5 notificaciones");
            verify(notificacionRepository).marcarTodasComoLeidas(eq("user-001"), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Debe retornar mensaje cuando no hay notificaciones pendientes")
        void marcarTodasComoLeidas_SinPendientes_RetornaMensaje() {
            // Arrange
            when(usuarioRepository.existsById("user-001")).thenReturn(true);
            when(notificacionRepository.contarNotificacionesNoLeidas("user-001")).thenReturn(0L);

            // Act
            MessageResponse resultado = notificacionService.marcarTodasComoLeidas("user-001");

            // Assert
            assertThat(resultado.getSuccess()).isTrue();
            assertThat(resultado.getMessage()).isEqualTo("No hay notificaciones pendientes por leer");
            verify(notificacionRepository, never()).marcarTodasComoLeidas(any(), any());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando usuario no existe")
        void marcarTodasComoLeidas_UsuarioNoExiste_LanzaExcepcion() {
            // Arrange
            when(usuarioRepository.existsById("user-999")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> notificacionService.marcarTodasComoLeidas("user-999"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Usuario no encontrado");
        }
    }

    // ==================== TESTS ELIMINAR NOTIFICACIONES ANTIGUAS ====================
    @Nested
    @DisplayName("Tests para eliminarNotificacionesAntiguas()")
    class EliminarNotificacionesAntiguasTests {

        @Test
        @DisplayName("Debe eliminar notificaciones antiguas y retornar cantidad")
        void eliminarNotificacionesAntiguas_ConAntiguas_EliminaYRetornaCantidad() {
            // Arrange
            when(notificacionRepository.count()).thenReturn(100L).thenReturn(95L);

            // Act
            Integer resultado = notificacionService.eliminarNotificacionesAntiguas(30);

            // Assert
            assertThat(resultado).isEqualTo(5);
            verify(notificacionRepository).eliminarNotificacionesAntiguas(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Debe retornar cero cuando no hay notificaciones antiguas")
        void eliminarNotificacionesAntiguas_SinAntiguas_RetornaCero() {
            // Arrange
            when(notificacionRepository.count()).thenReturn(50L).thenReturn(50L);

            // Act
            Integer resultado = notificacionService.eliminarNotificacionesAntiguas(30);

            // Assert
            assertThat(resultado).isZero();
        }

        @Test
        @DisplayName("Debe retornar cero cuando ocurre error")
        void eliminarNotificacionesAntiguas_Error_RetornaCero() {
            // Arrange
            when(notificacionRepository.count()).thenThrow(new RuntimeException("Error de BD"));

            // Act
            Integer resultado = notificacionService.eliminarNotificacionesAntiguas(30);

            // Assert
            assertThat(resultado).isZero();
        }
    }

    // ==================== TESTS METODOS AUXILIARES ====================
    @Nested
    @DisplayName("Tests para métodos auxiliares de notificación")
    class MetodosAuxiliaresTests {

        @Test
        @DisplayName("notificarNuevaReserva debe llamar a enviarNotificacion con datos correctos")
        void notificarNuevaReserva_LlamaEnviarNotificacion() {
            // Arrange
            when(usuarioRepository.findById("prov-001")).thenReturn(Optional.of(usuario));
            when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacion);

            // Act
            notificacionService.notificarNuevaReserva("prov-001", "Cancha Fútbol", "Juan Cliente");

            // Assert
            verify(notificacionRepository).save(argThat(n ->
                    n.getTipoNotificacion().equals("RESERVA") &&
                            n.getTitulo().equals("Nueva Reserva Recibida")
            ));
        }

        @Test
        @DisplayName("notificarReservaConfirmada debe llamar a enviarNotificacion")
        void notificarReservaConfirmada_LlamaEnviarNotificacion() {
            // Arrange
            when(usuarioRepository.findById("cli-001")).thenReturn(Optional.of(usuario));
            when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacion);

            // Act
            notificacionService.notificarReservaConfirmada("cli-001", "Cancha Fútbol", "2025-01-15");

            // Assert
            verify(notificacionRepository).save(argThat(n ->
                    n.getTipoNotificacion().equals("RESERVA") &&
                            n.getTitulo().equals("Reserva Confirmada")
            ));
        }

        @Test
        @DisplayName("notificarPagoAprobado debe llamar a enviarNotificacion")
        void notificarPagoAprobado_LlamaEnviarNotificacion() {
            // Arrange
            when(usuarioRepository.findById("cli-001")).thenReturn(Optional.of(usuario));
            when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacion);

            // Act
            notificacionService.notificarPagoAprobado("cli-001", "$50.000", "Cancha Fútbol");

            // Assert
            verify(notificacionRepository).save(argThat(n ->
                    n.getTipoNotificacion().equals("PAGO") &&
                            n.getTitulo().equals("Pago Aprobado")
            ));
        }

        @Test
        @DisplayName("notificarCancelacion debe llamar a enviarNotificacion")
        void notificarCancelacion_LlamaEnviarNotificacion() {
            // Arrange
            when(usuarioRepository.findById("user-001")).thenReturn(Optional.of(usuario));
            when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacion);

            // Act
            notificacionService.notificarCancelacion("user-001", "Cancha Fútbol", "Motivo personal");

            // Assert
            verify(notificacionRepository).save(argThat(n ->
                    n.getTipoNotificacion().equals("CANCELACION") &&
                            n.getTitulo().equals("Reserva Cancelada")
            ));
        }

        @Test
        @DisplayName("notificarNuevaResena debe llamar a enviarNotificacion")
        void notificarNuevaResena_LlamaEnviarNotificacion() {
            // Arrange
            when(usuarioRepository.findById("prov-001")).thenReturn(Optional.of(usuario));
            when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacion);

            // Act
            notificacionService.notificarNuevaResena("prov-001", "Cancha Fútbol", 5);

            // Assert
            verify(notificacionRepository).save(argThat(n ->
                    n.getTipoNotificacion().equals("RESENA") &&
                            n.getTitulo().equals("Nueva Reseña Recibida")
            ));
        }

        @Test
        @DisplayName("notificarSistema debe llamar a enviarNotificacion con tipo SISTEMA")
        void notificarSistema_LlamaEnviarNotificacion() {
            // Arrange
            when(usuarioRepository.findById("user-001")).thenReturn(Optional.of(usuario));
            when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacion);

            // Act
            notificacionService.notificarSistema("user-001", "Aviso Importante", "Mensaje del sistema");

            // Assert
            verify(notificacionRepository).save(argThat(n ->
                    n.getTipoNotificacion().equals("SISTEMA")
            ));
        }
    }
}