package com.sm_sport.service;

import com.sm_sport.dto.request.CancelarReservaRequest;
import com.sm_sport.dto.request.CrearReservaRequest;
import com.sm_sport.dto.request.FiltroReservaRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PageResponse;
import com.sm_sport.dto.response.ReservaDetalleResponse;
import com.sm_sport.dto.response.ReservaResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ForbiddenException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.PageMapper;
import com.sm_sport.mapper.ReservaMapper;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.model.enums.EstadoServicio;
import com.sm_sport.repository.ClienteRepository;
import com.sm_sport.repository.DisponibilidadServicioRepository;
import com.sm_sport.repository.ReservaRepository;
import com.sm_sport.repository.ServicioRepository;
import com.sm_sport.service.impl.ReservaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ReservaServiceImpl")
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ServicioRepository servicioRepository;
    @Mock
    private DisponibilidadServicioRepository disponibilidadRepository;
    @Mock
    private ReservaMapper reservaMapper;
    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    // Objetos de prueba reutilizables
    private Cliente cliente;
    private Proveedor proveedor;
    private Servicio servicio;
    private Reserva reserva;
    private CrearReservaRequest crearReservaRequest;
    private ReservaResponse reservaResponse;
    private DisponibilidadServicio disponibilidad;

    @BeforeEach
    void setUp() {
        // Configurar Proveedor
        proveedor = new Proveedor();
        proveedor.setIdUsuario("prov-001");
        proveedor.setNombre("Juan Proveedor");
        proveedor.setTotalReservasCompletadas(10);

        // Configurar Cliente
        cliente = new Cliente();
        cliente.setIdUsuario("cli-001");
        cliente.setNombre("María Cliente");

        // Configurar Servicio
        servicio = Servicio.builder()
                .idServicio("serv-001")
                .nombre("Cancha de Fútbol 5")
                .deporte("Fútbol")
                .precio(new BigDecimal("50000"))
                .estado(EstadoServicio.PUBLICADO)
                .proveedor(proveedor)
                .build();

        // Configurar Disponibilidad
        disponibilidad = DisponibilidadServicio.builder()
                .idDisponibilidad("disp-001")
                .servicio(servicio)
                .fecha(LocalDate.now().plusDays(1))
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(22, 0))
                .disponible(true)
                .cuposDisponibles(5)
                .build();

        // Configurar Request de creación
        crearReservaRequest = CrearReservaRequest.builder()
                .idServicio("serv-001")
                .fechaReserva(LocalDate.now().plusDays(1))
                .horaReserva(LocalTime.of(10, 0))
                .notasCliente("Necesito balones")
                .build();

        // Configurar Reserva
        reserva = Reserva.builder()
                .idReserva("res-001")
                .cliente(cliente)
                .servicio(servicio)
                .proveedor(proveedor)
                .fechaReserva(LocalDate.now().plusDays(1))
                .horaReserva(LocalTime.of(10, 0))
                .estado(EstadoReserva.PENDIENTE)
                .costoTotal(new BigDecimal("50000"))
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Configurar Response
        reservaResponse = ReservaResponse.builder()
                .idReserva("res-001")
                .fechaReserva(LocalDate.now().plusDays(1))
                .horaReserva(LocalTime.of(10, 0))
                .estado(EstadoReserva.PENDIENTE)
                .costoTotal(new BigDecimal("50000"))
                .idCliente("cli-001")
                .nombreCliente("María Cliente")
                .idServicio("serv-001")
                .nombreServicio("Cancha de Fútbol 5")
                .idProveedor("prov-001")
                .nombreProveedor("Juan Proveedor")
                .build();
    }

    // ==================== TESTS CREAR RESERVA ====================
    @Nested
    @DisplayName("Tests para crearReserva()")
    class CrearReservaTests {

        @Test
        @DisplayName("Debe crear reserva exitosamente cuando todos los datos son válidos")
        void crearReserva_DatosValidos_RetornaReservaResponse() {
            // Arrange
            when(clienteRepository.findById("cli-001")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById("serv-001")).thenReturn(Optional.of(servicio));
            when(disponibilidadRepository.verificarDisponibilidad(anyString(), any(), any()))
                    .thenReturn(true);
            when(reservaMapper.toEntity(crearReservaRequest)).thenReturn(reserva);
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
            when(disponibilidadRepository.findByServicioAndFecha(anyString(), any()))
                    .thenReturn(List.of(disponibilidad));
            when(reservaMapper.toResponse(reserva)).thenReturn(reservaResponse);

            // Act
            ReservaResponse resultado = reservaService.crearReserva("cli-001", crearReservaRequest);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getIdReserva()).isEqualTo("res-001");
            assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
            verify(reservaRepository).save(any(Reserva.class));
            verify(disponibilidadRepository).save(any(DisponibilidadServicio.class));
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el cliente no existe")
        void crearReserva_ClienteNoExiste_LanzaExcepcion() {
            // Arrange
            when(clienteRepository.findById("cli-999")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservaService.crearReserva("cli-999", crearReservaRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Cliente no encontrado");

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el servicio no existe")
        void crearReserva_ServicioNoExiste_LanzaExcepcion() {
            // Arrange
            when(clienteRepository.findById("cli-001")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById("serv-001")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservaService.crearReserva("cli-001", crearReservaRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Servicio no encontrado");

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando el servicio no está publicado")
        void crearReserva_ServicioNoPubicado_LanzaExcepcion() {
            // Arrange
            servicio.setEstado(EstadoServicio.PAUSADO);
            when(clienteRepository.findById("cli-001")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById("serv-001")).thenReturn(Optional.of(servicio));

            // Act & Assert
            assertThatThrownBy(() -> reservaService.crearReserva("cli-001", crearReservaRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("El servicio no está disponible");

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando no hay disponibilidad")
        void crearReserva_SinDisponibilidad_LanzaExcepcion() {
            // Arrange
            when(clienteRepository.findById("cli-001")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById("serv-001")).thenReturn(Optional.of(servicio));
            when(disponibilidadRepository.verificarDisponibilidad(anyString(), any(), any()))
                    .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> reservaService.crearReserva("cli-001", crearReservaRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("No hay disponibilidad para la fecha y hora seleccionadas");

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe reducir cupos y marcar como no disponible cuando cupos llegan a 0")
        void crearReserva_UltimoCupo_MarcaNoDisponible() {
            // Arrange
            disponibilidad.setCuposDisponibles(1);
            when(clienteRepository.findById("cli-001")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById("serv-001")).thenReturn(Optional.of(servicio));
            when(disponibilidadRepository.verificarDisponibilidad(anyString(), any(), any()))
                    .thenReturn(true);
            when(reservaMapper.toEntity(crearReservaRequest)).thenReturn(reserva);
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
            when(disponibilidadRepository.findByServicioAndFecha(anyString(), any()))
                    .thenReturn(List.of(disponibilidad));
            when(reservaMapper.toResponse(reserva)).thenReturn(reservaResponse);

            // Act
            reservaService.crearReserva("cli-001", crearReservaRequest);

            // Assert
            assertThat(disponibilidad.getCuposDisponibles()).isZero();
            assertThat(disponibilidad.getDisponible()).isFalse();
            verify(disponibilidadRepository).save(disponibilidad);
        }
    }

    // ==================== TESTS OBTENER RESERVA ====================
    @Nested
    @DisplayName("Tests para obtenerPorId()")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Debe retornar reserva cuando existe")
        void obtenerPorId_ReservaExiste_RetornaResponse() {
            // Arrange
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));
            when(reservaMapper.toResponse(reserva)).thenReturn(reservaResponse);

            // Act
            ReservaResponse resultado = reservaService.obtenerPorId("res-001");

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getIdReserva()).isEqualTo("res-001");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando reserva no existe")
        void obtenerPorId_ReservaNoExiste_LanzaExcepcion() {
            // Arrange
            when(reservaRepository.findById("res-999")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservaService.obtenerPorId("res-999"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Reserva no encontrada");
        }
    }

    // ==================== TESTS OBTENER DETALLE ====================
    @Nested
    @DisplayName("Tests para obtenerDetalle()")
    class ObtenerDetalleTests {

        @Test
        @DisplayName("Debe retornar detalle completo cuando reserva existe")
        void obtenerDetalle_ReservaExiste_RetornaDetalle() {
            // Arrange
            ReservaDetalleResponse detalleResponse = new ReservaDetalleResponse();
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));
            when(reservaMapper.toDetalleResponse(reserva)).thenReturn(detalleResponse);

            // Act
            ReservaDetalleResponse resultado = reservaService.obtenerDetalle("res-001");

            // Assert
            assertThat(resultado).isNotNull();
            verify(reservaMapper).toDetalleResponse(reserva);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando reserva no existe")
        void obtenerDetalle_ReservaNoExiste_LanzaExcepcion() {
            // Arrange
            when(reservaRepository.findById("res-999")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservaService.obtenerDetalle("res-999"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Reserva no encontrada");
        }
    }

    // ==================== TESTS LISTAR POR CLIENTE ====================
    @Nested
    @DisplayName("Tests para listarPorCliente()")
    class ListarPorClienteTests {

        @Test
        @DisplayName("Debe retornar lista paginada de reservas del cliente")
        void listarPorCliente_ClienteConReservas_RetornaPaginado() {
            // Arrange
            Page<Reserva> pageReservas = new PageImpl<>(List.of(reserva));
            PageResponse<ReservaResponse> pageResponse = PageResponse.<ReservaResponse>builder()
                    .content(List.of(reservaResponse))
                    .totalElements(1L)
                    .totalPages(1)
                    .build();

            when(reservaRepository.findByClienteIdUsuario(eq("cli-001"), any(Pageable.class)))
                    .thenReturn(pageReservas);
            when(reservaMapper.toResponse(reserva)).thenReturn(reservaResponse);
            doReturn(pageResponse).when(pageMapper).toPageResponse(any());

            // Act
            PageResponse<ReservaResponse> resultado = reservaService
                    .listarPorCliente("cli-001", 0, 10);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando cliente no tiene reservas")
        void listarPorCliente_SinReservas_RetornaListaVacia() {
            // Arrange
            Page<Reserva> pageVacia = new PageImpl<>(Collections.emptyList());
            PageResponse<ReservaResponse> pageResponse = PageResponse.<ReservaResponse>builder()
                    .content(Collections.emptyList())
                    .totalElements(0L)
                    .empty(true)
                    .build();

            when(reservaRepository.findByClienteIdUsuario(eq("cli-001"), any(Pageable.class)))
                    .thenReturn(pageVacia);
            doReturn(pageResponse).when(pageMapper).toPageResponse(any());

            // Act
            PageResponse<ReservaResponse> resultado = reservaService
                    .listarPorCliente("cli-001", 0, 10);

            // Assert
            assertThat(resultado.getContent()).isEmpty();
            assertThat(resultado.getTotalElements()).isZero();
        }
    }

    // ==================== TESTS LISTAR POR PROVEEDOR ====================
    @Nested
    @DisplayName("Tests para listarPorProveedor()")
    class ListarPorProveedorTests {

        @Test
        @DisplayName("Debe retornar lista paginada de reservas del proveedor")
        void listarPorProveedor_ProveedorConReservas_RetornaPaginado() {
            // Arrange
            Page<Reserva> pageReservas = new PageImpl<>(List.of(reserva));
            PageResponse<ReservaResponse> pageResponse = PageResponse.<ReservaResponse>builder()
                    .content(List.of(reservaResponse))
                    .totalElements(1L)
                    .build();

            when(reservaRepository.findByProveedorIdUsuario(eq("prov-001"), any(Pageable.class)))
                    .thenReturn(pageReservas);
            when(reservaMapper.toResponse(reserva)).thenReturn(reservaResponse);
            doReturn(pageResponse).when(pageMapper).toPageResponse(any());

            // Act
            PageResponse<ReservaResponse> resultado = reservaService
                    .listarPorProveedor("prov-001", 0, 10);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
        }
    }

    // ==================== TESTS CONFIRMAR RESERVA ====================
    @Nested
    @DisplayName("Tests para confirmarReserva()")
    class ConfirmarReservaTests {

        @Test
        @DisplayName("Debe confirmar reserva exitosamente cuando proveedor es dueño")
        void confirmarReserva_ProveedorDueno_ConfirmaExitosamente() {
            // Arrange
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

            ReservaResponse responseConfirmada = ReservaResponse.builder()
                    .idReserva("res-001")
                    .estado(EstadoReserva.CONFIRMADA)
                    .build();
            when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseConfirmada);

            // Act
            ReservaResponse resultado = reservaService.confirmarReserva("res-001", "prov-001");

            // Assert
            assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
            verify(reservaRepository).save(argThat(r ->
                    r.getEstado() == EstadoReserva.CONFIRMADA));
        }

        @Test
        @DisplayName("Debe lanzar ForbiddenException cuando proveedor no es dueño")
        void confirmarReserva_ProveedorNoDueno_LanzaExcepcion() {
            // Arrange
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));

            // Act & Assert
            assertThatThrownBy(() ->
                    reservaService.confirmarReserva("res-001", "otro-proveedor"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("No tienes permiso para confirmar esta reserva");

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando reserva no está pendiente")
        void confirmarReserva_EstadoInvalido_LanzaExcepcion() {
            // Arrange
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));

            // Act & Assert
            assertThatThrownBy(() ->
                    reservaService.confirmarReserva("res-001", "prov-001"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Solo se pueden confirmar reservas pendientes");

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando reserva no existe")
        void confirmarReserva_ReservaNoExiste_LanzaExcepcion() {
            // Arrange
            when(reservaRepository.findById("res-999")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    reservaService.confirmarReserva("res-999", "prov-001"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Reserva no encontrada");
        }
    }

    // ==================== TESTS RECHAZAR RESERVA ====================
    @Nested
    @DisplayName("Tests para rechazarReserva()")
    class RechazarReservaTests {

        @Test
        @DisplayName("Debe rechazar reserva exitosamente")
        void rechazarReserva_ProveedorDueno_RechazaExitosamente() {
            // Arrange
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

            ReservaResponse responseRechazada = ReservaResponse.builder()
                    .idReserva("res-001")
                    .estado(EstadoReserva.RECHAZADA)
                    .build();
            when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseRechazada);

            // Act
            ReservaResponse resultado = reservaService
                    .rechazarReserva("res-001", "prov-001", "Sin disponibilidad");

            // Assert
            assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.RECHAZADA);
            verify(reservaRepository).save(argThat(r ->
                    r.getEstado() == EstadoReserva.RECHAZADA));
        }

        @Test
        @DisplayName("Debe lanzar ForbiddenException cuando proveedor no es dueño")
        void rechazarReserva_ProveedorNoDueno_LanzaExcepcion() {
            // Arrange
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));

            // Act & Assert
            assertThatThrownBy(() ->
                    reservaService.rechazarReserva("res-001", "otro-proveedor", "Motivo"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("No tienes permiso para rechazar esta reserva");
        }
    }

    // ==================== TESTS CANCELAR RESERVA ====================
    @Nested
    @DisplayName("Tests para cancelarReserva()")
    class CancelarReservaTests {

        private CancelarReservaRequest cancelarRequest;

        @BeforeEach
        void setUpCancelar() {
            cancelarRequest = new CancelarReservaRequest();
            cancelarRequest.setMotivoCancelacion("Ya no puedo asistir");
            cancelarRequest.setSolicitarReembolso(false);
        }

        @Test
        @DisplayName("Debe cancelar reserva exitosamente cuando cliente es dueño")
        void cancelarReserva_ClienteDueno_CancelaExitosamente() {
            // Arrange
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));

            // Act
            MessageResponse resultado = reservaService
                    .cancelarReserva("res-001", "cli-001", cancelarRequest);

            // Assert
            assertThat(resultado.getSuccess()).isTrue();
            assertThat(resultado.getMessage()).isEqualTo("Reserva cancelada exitosamente");
            verify(reservaRepository).save(argThat(r ->
                    r.getEstado() == EstadoReserva.CANCELADA &&
                            r.getNotasCliente().equals("Ya no puedo asistir")));
        }

        @Test
        @DisplayName("Debe lanzar ForbiddenException cuando cliente no es dueño")
        void cancelarReserva_ClienteNoDueno_LanzaExcepcion() {
            // Arrange
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));

            // Act & Assert
            assertThatThrownBy(() ->
                    reservaService.cancelarReserva("res-001", "otro-cliente", cancelarRequest))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("No tienes permiso para cancelar esta reserva");

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe lanzar BusinessException cuando reserva está finalizada")
        void cancelarReserva_ReservaFinalizada_LanzaExcepcion() {
            // Arrange
            reserva.setEstado(EstadoReserva.FINALIZADA);
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));

            // Act & Assert
            assertThatThrownBy(() ->
                    reservaService.cancelarReserva("res-001", "cli-001", cancelarRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("No se puede cancelar una reserva finalizada");

            verify(reservaRepository, never()).save(any());
        }
    }

    // ==================== TESTS FINALIZAR RESERVA ====================
    @Nested
    @DisplayName("Tests para finalizarReserva()")
    class FinalizarReservaTests {

        @Test
        @DisplayName("Debe finalizar reserva y actualizar contador del proveedor")
        void finalizarReserva_ReservaExiste_FinalizaExitosamente() {
            // Arrange
            int reservasAntes = proveedor.getTotalReservasCompletadas();
            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

            ReservaResponse responseFinalizada = ReservaResponse.builder()
                    .idReserva("res-001")
                    .estado(EstadoReserva.FINALIZADA)
                    .build();
            when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseFinalizada);

            // Act
            ReservaResponse resultado = reservaService.finalizarReserva("res-001");

            // Assert
            assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.FINALIZADA);
            assertThat(proveedor.getTotalReservasCompletadas()).isEqualTo(reservasAntes + 1);
            verify(reservaRepository).save(argThat(r ->
                    r.getEstado() == EstadoReserva.FINALIZADA));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando reserva no existe")
        void finalizarReserva_ReservaNoExiste_LanzaExcepcion() {
            // Arrange
            when(reservaRepository.findById("res-999")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> reservaService.finalizarReserva("res-999"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Reserva no encontrada");
        }
    }

    // ==================== TESTS VERIFICAR DISPONIBILIDAD ====================
    @Nested
    @DisplayName("Tests para verificarDisponibilidad()")
    class VerificarDisponibilidadTests {

        @Test
        @DisplayName("Debe retornar true cuando hay disponibilidad")
        void verificarDisponibilidad_HayDisponibilidad_RetornaTrue() {
            // Arrange
            when(disponibilidadRepository.verificarDisponibilidad(
                    eq("serv-001"), any(LocalDate.class), any(LocalTime.class)))
                    .thenReturn(true);

            // Act
            boolean resultado = reservaService
                    .verificarDisponibilidad("serv-001", crearReservaRequest);

            // Assert
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false cuando no hay disponibilidad")
        void verificarDisponibilidad_NoHayDisponibilidad_RetornaFalse() {
            // Arrange
            when(disponibilidadRepository.verificarDisponibilidad(
                    eq("serv-001"), any(LocalDate.class), any(LocalTime.class)))
                    .thenReturn(false);

            // Act
            boolean resultado = reservaService
                    .verificarDisponibilidad("serv-001", crearReservaRequest);

            // Assert
            assertThat(resultado).isFalse();
        }
    }

    // ==================== TESTS CANCELAR RESERVAS EXPIRADAS ====================
    @Nested
    @DisplayName("Tests para cancelarReservasExpiradas()")
    class CancelarReservasExpiradasTests {

        @Test
        @DisplayName("Debe cancelar reservas pendientes antiguas y retornar cantidad")
        void cancelarReservasExpiradas_HayReservasExpiradas_CancelaYRetornaCantidad() {
            // Arrange
            Reserva reservaExpirada1 = Reserva.builder()
                    .idReserva("res-exp-001")
                    .estado(EstadoReserva.PENDIENTE)
                    .cliente(cliente)
                    .build();
            Reserva reservaExpirada2 = Reserva.builder()
                    .idReserva("res-exp-002")
                    .estado(EstadoReserva.PENDIENTE)
                    .cliente(cliente)
                    .build();

            List<Reserva> reservasExpiradas = List.of(reservaExpirada1, reservaExpirada2);
            when(reservaRepository.findReservasPendientesAntiguas(any(LocalDateTime.class)))
                    .thenReturn(reservasExpiradas);

            // Act
            Integer resultado = reservaService.cancelarReservasExpiradas();

            // Assert
            assertThat(resultado).isEqualTo(2);
            assertThat(reservaExpirada1.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
            assertThat(reservaExpirada2.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
            verify(reservaRepository).saveAll(reservasExpiradas);
        }

        @Test
        @DisplayName("Debe retornar cero cuando no hay reservas expiradas")
        void cancelarReservasExpiradas_NoHayExpiradas_RetornaCero() {
            // Arrange
            when(reservaRepository.findReservasPendientesAntiguas(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // Act
            Integer resultado = reservaService.cancelarReservasExpiradas();

            // Assert
            assertThat(resultado).isZero();
            verify(reservaRepository).saveAll(Collections.emptyList());
        }
    }

    // ==================== TESTS FILTRAR RESERVAS ====================
    @Nested
    @DisplayName("Tests para filtrarReservas()")
    class FiltrarReservasTests {

        @Test
        @DisplayName("Debe filtrar reservas con paginación y ordenamiento")
        void filtrarReservas_ConFiltros_RetornaPaginado() {
            // Arrange
            FiltroReservaRequest filtros = FiltroReservaRequest.builder()
                    .pagina(0)
                    .tamano(10)
                    .ordenarPor("fechaReserva")
                    .direccion("DESC")
                    .build();

            Page<Reserva> pageReservas = new PageImpl<>(List.of(reserva));
            PageResponse<ReservaResponse> pageResponse = PageResponse.<ReservaResponse>builder()
                    .content(List.of(reservaResponse))
                    .totalElements(1L)
                    .pageNumber(0)
                    .pageSize(10)
                    .build();

            when(reservaRepository.findAll(any(Pageable.class))).thenReturn(pageReservas);
            when(reservaMapper.toResponse(reserva)).thenReturn(reservaResponse);
            doReturn(pageResponse).when(pageMapper).toPageResponse(any());

            // Act
            PageResponse<ReservaResponse> resultado = reservaService.filtrarReservas(filtros);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getPageNumber()).isZero();
            assertThat(resultado.getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("Debe aplicar ordenamiento ascendente correctamente")
        void filtrarReservas_OrdenamientoAscendente_AplicaCorrectamente() {
            // Arrange
            FiltroReservaRequest filtros = FiltroReservaRequest.builder()
                    .pagina(0)
                    .tamano(20)
                    .ordenarPor("costoTotal")
                    .direccion("ASC")
                    .build();

            Page<Reserva> pageReservas = new PageImpl<>(Collections.emptyList());
            PageResponse<ReservaResponse> pageResponse = PageResponse.<ReservaResponse>builder()
                    .content(Collections.emptyList())
                    .build();

            when(reservaRepository.findAll(any(Pageable.class))).thenReturn(pageReservas);
            doReturn(pageResponse).when(pageMapper).toPageResponse(any());

            // Act
            reservaService.filtrarReservas(filtros);

            // Assert
            verify(reservaRepository).findAll(argThat((Pageable p) ->
                    p.getSort().getOrderFor("costoTotal") != null &&
                            p.getSort().getOrderFor("costoTotal").getDirection() == Sort.Direction.ASC
            ));
        }
    }

    // ==================== TESTS ADICIONALES DE EDGE CASES ====================
    @Nested
    @DisplayName("Tests de casos límite")
    class EdgeCasesTests {

        @Test
        @DisplayName("Crear reserva sin notas del cliente debe funcionar")
        void crearReserva_SinNotasCliente_CreaExitosamente() {
            // Arrange
            crearReservaRequest.setNotasCliente(null);
            when(clienteRepository.findById("cli-001")).thenReturn(Optional.of(cliente));
            when(servicioRepository.findById("serv-001")).thenReturn(Optional.of(servicio));
            when(disponibilidadRepository.verificarDisponibilidad(anyString(), any(), any()))
                    .thenReturn(true);
            when(reservaMapper.toEntity(crearReservaRequest)).thenReturn(reserva);
            when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
            when(disponibilidadRepository.findByServicioAndFecha(anyString(), any()))
                    .thenReturn(Collections.emptyList());
            when(reservaMapper.toResponse(reserva)).thenReturn(reservaResponse);

            // Act
            ReservaResponse resultado = reservaService.crearReserva("cli-001", crearReservaRequest);

            // Assert
            assertThat(resultado).isNotNull();
            verify(reservaRepository).save(any(Reserva.class));
        }

        @Test
        @DisplayName("Cancelar reserva confirmada debe permitirse")
        void cancelarReserva_ReservaConfirmada_PermiteCancelacion() {
            // Arrange
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            CancelarReservaRequest cancelarRequest = new CancelarReservaRequest();
            cancelarRequest.setMotivoCancelacion("Emergencia personal");

            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));

            // Act
            MessageResponse resultado = reservaService
                    .cancelarReserva("res-001", "cli-001", cancelarRequest);

            // Assert
            assertThat(resultado.getSuccess()).isTrue();
            verify(reservaRepository).save(argThat(r ->
                    r.getEstado() == EstadoReserva.CANCELADA));
        }

        @Test
        @DisplayName("Cancelar reserva rechazada debe permitirse")
        void cancelarReserva_ReservaRechazada_PermiteCancelacion() {
            // Arrange
            reserva.setEstado(EstadoReserva.RECHAZADA);
            CancelarReservaRequest cancelarRequest = new CancelarReservaRequest();
            cancelarRequest.setMotivoCancelacion("Ya fue rechazada");

            when(reservaRepository.findById("res-001")).thenReturn(Optional.of(reserva));

            // Act
            MessageResponse resultado = reservaService
                    .cancelarReserva("res-001", "cli-001", cancelarRequest);

            // Assert
            assertThat(resultado.getSuccess()).isTrue();
        }
    }
}