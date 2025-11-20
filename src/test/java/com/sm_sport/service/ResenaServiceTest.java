package com.sm_sport.service;

import com.sm_sport.dto.request.CrearResenaRequest;
import com.sm_sport.dto.request.ResponderResenaRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.ResenaResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.ResenaMapper;
import com.sm_sport.model.entity.*;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.model.enums.EstadoRevision;
import com.sm_sport.repository.*;
import com.sm_sport.service.impl.ResenaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ResenaService")
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private ResenaMapper resenaMapper;

    @InjectMocks
    private ResenaServiceImpl resenaService;

    private Cliente clienteMock;
    private Proveedor proveedorMock;
    private Servicio servicioMock;
    private Reserva reservaMock;
    private Resena resenaMock;
    private CrearResenaRequest crearResenaRequest;
    private ResenaResponse resenaResponse;

    @BeforeEach
    void setUp() {
        // Cliente mock
        clienteMock = new Cliente();
        clienteMock.setIdUsuario("cliente-123");
        clienteMock.setNombre("Juan Pérez");
        clienteMock.setCorreo("juan@test.com");

        // Proveedor mock
        proveedorMock = new Proveedor();
        proveedorMock.setIdUsuario("proveedor-456");
        proveedorMock.setNombre("Carlos Martínez");
        proveedorMock.setCalificacionPromedio(BigDecimal.ZERO);
        proveedorMock.setServiciosPublicados(new ArrayList<>());

        // Servicio mock
        servicioMock = new Servicio();
        servicioMock.setIdServicio("servicio-789");
        servicioMock.setNombre("Cancha de Fútbol 5");
        servicioMock.setProveedor(proveedorMock);
        servicioMock.setCalificacionPromedio(BigDecimal.ZERO);
        servicioMock.setTotalResenas(0);
        servicioMock.setResenas(new ArrayList<>());

        // Agregar servicio a la lista del proveedor
        proveedorMock.getServiciosPublicados().add(servicioMock);

        // Reserva mock
        reservaMock = new Reserva();
        reservaMock.setIdReserva("reserva-111");
        reservaMock.setCliente(clienteMock);
        reservaMock.setServicio(servicioMock);
        reservaMock.setProveedor(proveedorMock);
        reservaMock.setEstado(EstadoReserva.FINALIZADA);
        reservaMock.setFechaReserva(LocalDate.now().minusDays(1));
        reservaMock.setHoraReserva(LocalTime.of(10, 0));
        reservaMock.setCostoTotal(BigDecimal.valueOf(50000));

        // Request mock
        crearResenaRequest = new CrearResenaRequest();
        crearResenaRequest.setIdReserva("reserva-111");
        crearResenaRequest.setCalificacion(5);
        crearResenaRequest.setComentario("Excelente servicio, muy recomendado");

        // Reseña mock
        resenaMock = new Resena();
        resenaMock.setIdResena("resena-222");
        resenaMock.setCliente(clienteMock);
        resenaMock.setServicio(servicioMock);
        resenaMock.setReserva(reservaMock);
        resenaMock.setCalificacion(5);
        resenaMock.setComentario("Excelente servicio, muy recomendado");
        resenaMock.setFechaCreacion(LocalDateTime.now());
        resenaMock.setReportada(false);
        resenaMock.setEstadoRevision(EstadoRevision.PUBLICADA);

        // Response mock
        resenaResponse = ResenaResponse.builder()
                .idResena("resena-222")
                .calificacion(5)
                .comentario("Excelente servicio, muy recomendado")
                .fechaCreacion(LocalDateTime.now())
                .reportada(false)
                .estadoRevision(EstadoRevision.PUBLICADA)
                .idCliente("cliente-123")
                .nombreCliente("Juan Pérez")
                .idServicio("servicio-789")
                .nombreServicio("Cancha de Fútbol 5")
                .build();
    }

    // ==================== TESTS DE CREAR RESEÑA ====================

    @Test
    @DisplayName("Crear reseña - Exitoso")
    void crearResena_Exitoso() {
        // Arrange
        when(clienteRepository.findById(anyString())).thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(anyString())).thenReturn(Optional.of(reservaMock));
        when(resenaRepository.findByReservaIdReserva(anyString())).thenReturn(Optional.empty());
        when(resenaMapper.toEntity(any(CrearResenaRequest.class))).thenReturn(resenaMock);
        when(resenaRepository.save(any(Resena.class))).thenReturn(resenaMock);
        when(resenaMapper.toResponse(any(Resena.class))).thenReturn(resenaResponse);

        // Mocks para actualizarCalificacionServicio
        when(servicioRepository.findById(anyString())).thenReturn(Optional.of(servicioMock));
        when(resenaRepository.calcularPromedioCalificacion(anyString())).thenReturn(BigDecimal.valueOf(5.0));
        when(resenaRepository.contarResenasPorServicio(anyString())).thenReturn(1L);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicioMock);

        // Mocks para actualizarCalificacionProveedor - USA ID ESPECÍFICO
        when(proveedorRepository.findById(anyString())).thenReturn(Optional.of(proveedorMock));
        when(resenaRepository.findResenasByServicio("servicio-789")).thenReturn(List.of(resenaMock));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorMock);

        // Act
        ResenaResponse resultado = resenaService.crearResena("cliente-123", crearResenaRequest);

        // Assert
        assertNotNull(resultado);
        assertEquals("resena-222", resultado.getIdResena());
        assertEquals(5, resultado.getCalificacion());
        assertEquals("Excelente servicio, muy recomendado", resultado.getComentario());

        verify(clienteRepository).findById("cliente-123");
        verify(reservaRepository).findById("reserva-111");
        verify(resenaRepository).findByReservaIdReserva("reserva-111");
        verify(resenaRepository).save(any(Resena.class));
        verify(servicioRepository).save(any(Servicio.class));
        verify(resenaRepository).findResenasByServicio("servicio-789");
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Crear reseña - Cliente no encontrado")
    void crearResena_ClienteNoEncontrado() {
        // Arrange
        when(clienteRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.crearResena("cliente-inexistente", crearResenaRequest)
        );

        assertEquals("Cliente no encontrado con ID: cliente-inexistente", exception.getMessage());
        verify(clienteRepository).findById("cliente-inexistente");
        verify(reservaRepository, never()).findById(anyString());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Crear reseña - Reserva no encontrada")
    void crearResena_ReservaNoEncontrada() {
        // Arrange
        when(clienteRepository.findById(anyString())).thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.crearResena("cliente-123", crearResenaRequest)
        );

        assertEquals("Reserva no encontrada con ID: reserva-111", exception.getMessage());
        verify(reservaRepository).findById("reserva-111");
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Crear reseña - Reserva no pertenece al cliente")
    void crearResena_ReservaNoPertenece() {
        // Arrange
        Cliente otroCliente = new Cliente();
        otroCliente.setIdUsuario("otro-cliente");
        reservaMock.setCliente(otroCliente);

        when(clienteRepository.findById(anyString())).thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(anyString())).thenReturn(Optional.of(reservaMock));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resenaService.crearResena("cliente-123", crearResenaRequest)
        );

        assertEquals("La reserva no pertenece al cliente", exception.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Crear reseña - Reserva no está finalizada")
    void crearResena_ReservaNoFinalizada() {
        // Arrange
        reservaMock.setEstado(EstadoReserva.CONFIRMADA);

        when(clienteRepository.findById(anyString())).thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(anyString())).thenReturn(Optional.of(reservaMock));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resenaService.crearResena("cliente-123", crearResenaRequest)
        );

        assertEquals("Solo se pueden calificar reservas finalizadas", exception.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Crear reseña - Ya existe reseña para esta reserva")
    void crearResena_ResenaDuplicada() {
        // Arrange
        when(clienteRepository.findById(anyString())).thenReturn(Optional.of(clienteMock));
        when(reservaRepository.findById(anyString())).thenReturn(Optional.of(reservaMock));
        when(resenaRepository.findByReservaIdReserva(anyString())).thenReturn(Optional.of(resenaMock));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resenaService.crearResena("cliente-123", crearResenaRequest)
        );

        assertEquals("Ya existe una reseña para esta reserva", exception.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    // ==================== TESTS DE OBTENER POR ID ====================

    @Test
    @DisplayName("Obtener reseña por ID - Exitoso")
    void obtenerPorId_Exitoso() {
        // Arrange
        when(resenaRepository.findById(anyString())).thenReturn(Optional.of(resenaMock));
        when(resenaMapper.toResponse(any(Resena.class))).thenReturn(resenaResponse);

        // Act
        ResenaResponse resultado = resenaService.obtenerPorId("resena-222");

        // Assert
        assertNotNull(resultado);
        assertEquals("resena-222", resultado.getIdResena());
        verify(resenaRepository).findById("resena-222");
        verify(resenaMapper).toResponse(resenaMock);
    }

    @Test
    @DisplayName("Obtener reseña por ID - No encontrada")
    void obtenerPorId_NoEncontrada() {
        // Arrange
        when(resenaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.obtenerPorId("resena-inexistente")
        );

        assertEquals("Reseña no encontrada con ID: resena-inexistente", exception.getMessage());
        verify(resenaRepository).findById("resena-inexistente");
    }

    // ==================== TESTS DE LISTAR POR SERVICIO ====================

    @Test
    @DisplayName("Listar reseñas por servicio - Exitoso")
    void listarPorServicio_Exitoso() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Resena> resenas = List.of(resenaMock);
        Page<Resena> pageResenas = new PageImpl<>(resenas, pageable, 1);

        when(servicioRepository.existsById(anyString())).thenReturn(true);
        when(resenaRepository.findByServicioIdServicio(anyString(), any(Pageable.class))).thenReturn(pageResenas);
        when(resenaMapper.toResponse(any(Resena.class))).thenReturn(resenaResponse);

        // Act
        Page<ResenaResponse> resultado = resenaService.listarPorServicio("servicio-789", pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        verify(servicioRepository).existsById("servicio-789");
        verify(resenaRepository).findByServicioIdServicio("servicio-789", pageable);
    }

    @Test
    @DisplayName("Listar reseñas por servicio - Servicio no encontrado")
    void listarPorServicio_ServicioNoEncontrado() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(servicioRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.listarPorServicio("servicio-inexistente", pageable)
        );

        assertEquals("Servicio no encontrado con ID: servicio-inexistente", exception.getMessage());
        verify(servicioRepository).existsById("servicio-inexistente");
        verify(resenaRepository, never()).findByServicioIdServicio(anyString(), any(Pageable.class));
    }

    // ==================== TESTS DE LISTAR POR CLIENTE ====================

    @Test
    @DisplayName("Listar reseñas por cliente - Exitoso")
    void listarPorCliente_Exitoso() {
        // Arrange
        List<Resena> resenas = List.of(resenaMock);
        List<ResenaResponse> responses = List.of(resenaResponse);

        when(clienteRepository.existsById(anyString())).thenReturn(true);
        when(resenaRepository.findByClienteIdUsuario(anyString())).thenReturn(resenas);
        when(resenaMapper.toResponseList(anyList())).thenReturn(responses);

        // Act
        List<ResenaResponse> resultado = resenaService.listarPorCliente("cliente-123");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(clienteRepository).existsById("cliente-123");
        verify(resenaRepository).findByClienteIdUsuario("cliente-123");
    }

    @Test
    @DisplayName("Listar reseñas por cliente - Cliente no encontrado")
    void listarPorCliente_ClienteNoEncontrado() {
        // Arrange
        when(clienteRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.listarPorCliente("cliente-inexistente")
        );

        assertEquals("Cliente no encontrado con ID: cliente-inexistente", exception.getMessage());
        verify(resenaRepository, never()).findByClienteIdUsuario(anyString());
    }

    // ==================== TESTS DE RESPONDER RESEÑA ====================

    @Test
    @DisplayName("Responder reseña - Exitoso")
    void responderResena_Exitoso() {
        // Arrange
        ResponderResenaRequest request = new ResponderResenaRequest();
        request.setRespuesta("Gracias por tu comentario. Nos alegra que hayas disfrutado el servicio.");

        when(proveedorRepository.findById(anyString())).thenReturn(Optional.of(proveedorMock));
        when(resenaRepository.findById(anyString())).thenReturn(Optional.of(resenaMock));
        when(resenaRepository.save(any(Resena.class))).thenReturn(resenaMock);
        when(resenaMapper.toResponse(any(Resena.class))).thenReturn(resenaResponse);

        // Act
        ResenaResponse resultado = resenaService.responderResena("resena-222", "proveedor-456", request);

        // Assert
        assertNotNull(resultado);
        verify(proveedorRepository).findById("proveedor-456");
        verify(resenaRepository).findById("resena-222");
        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    @DisplayName("Responder reseña - Proveedor no encontrado")
    void responderResena_ProveedorNoEncontrado() {
        // Arrange
        ResponderResenaRequest request = new ResponderResenaRequest();
        request.setRespuesta("Respuesta del proveedor");

        when(proveedorRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.responderResena("resena-222", "proveedor-inexistente", request)
        );

        assertEquals("Proveedor no encontrado con ID: proveedor-inexistente", exception.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Responder reseña - Reseña no encontrada")
    void responderResena_ResenaNoEncontrada() {
        // Arrange
        ResponderResenaRequest request = new ResponderResenaRequest();
        request.setRespuesta("Respuesta del proveedor");

        when(proveedorRepository.findById(anyString())).thenReturn(Optional.of(proveedorMock));
        when(resenaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.responderResena("resena-inexistente", "proveedor-456", request)
        );

        assertEquals("Reseña no encontrada con ID: resena-inexistente", exception.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Responder reseña - Servicio no pertenece al proveedor")
    void responderResena_ServicioNoPertenece() {
        // Arrange
        ResponderResenaRequest request = new ResponderResenaRequest();
        request.setRespuesta("Respuesta del proveedor");

        Proveedor otroProveedor = new Proveedor();
        otroProveedor.setIdUsuario("otro-proveedor");
        servicioMock.setProveedor(otroProveedor);

        when(proveedorRepository.findById(anyString())).thenReturn(Optional.of(proveedorMock));
        when(resenaRepository.findById(anyString())).thenReturn(Optional.of(resenaMock));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resenaService.responderResena("resena-222", "proveedor-456", request)
        );

        assertEquals("Solo el proveedor dueño del servicio puede responder esta reseña", exception.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Responder reseña - Ya tiene respuesta")
    void responderResena_YaTieneRespuesta() {
        // Arrange
        ResponderResenaRequest request = new ResponderResenaRequest();
        request.setRespuesta("Respuesta del proveedor");

        resenaMock.setRespuestaProveedor("Respuesta anterior");

        when(proveedorRepository.findById(anyString())).thenReturn(Optional.of(proveedorMock));
        when(resenaRepository.findById(anyString())).thenReturn(Optional.of(resenaMock));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resenaService.responderResena("resena-222", "proveedor-456", request)
        );

        assertEquals("Esta reseña ya tiene una respuesta", exception.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    // ==================== TESTS DE REPORTAR RESEÑA ====================

    @Test
    @DisplayName("Reportar reseña - Exitoso")
    void reportarResena_Exitoso() {
        // Arrange
        when(resenaRepository.findById(anyString())).thenReturn(Optional.of(resenaMock));
        when(resenaRepository.save(any(Resena.class))).thenReturn(resenaMock);

        // Act
        MessageResponse resultado = resenaService.reportarResena("resena-222", "usuario-999");

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getMessage().contains("reportada exitosamente"));
        verify(resenaRepository).findById("resena-222");
        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    @DisplayName("Reportar reseña - Reseña no encontrada")
    void reportarResena_NoEncontrada() {
        // Arrange
        when(resenaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.reportarResena("resena-inexistente", "usuario-999")
        );

        assertEquals("Reseña no encontrada con ID: resena-inexistente", exception.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Reportar reseña - Ya reportada")
    void reportarResena_YaReportada() {
        // Arrange
        resenaMock.setReportada(true);
        resenaMock.setEstadoRevision(EstadoRevision.EN_REVISION);

        when(resenaRepository.findById(anyString())).thenReturn(Optional.of(resenaMock));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resenaService.reportarResena("resena-222", "usuario-999")
        );

        assertEquals("Esta reseña ya ha sido reportada y está en revisión", exception.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    // ==================== TESTS DE ELIMINAR RESEÑA ====================

    @Test
    @DisplayName("Eliminar reseña - Exitoso")
    void eliminarResena_Exitoso() {
        // Arrange
        // Configurar el proveedor con la lista de servicios
        proveedorMock.getServiciosPublicados().clear();
        proveedorMock.getServiciosPublicados().add(servicioMock);

        when(resenaRepository.findById(anyString())).thenReturn(Optional.of(resenaMock));

        // Mocks para actualizarCalificacionServicio
        when(servicioRepository.findById(anyString())).thenReturn(Optional.of(servicioMock));
        when(resenaRepository.calcularPromedioCalificacion(anyString())).thenReturn(BigDecimal.ZERO);
        when(resenaRepository.contarResenasPorServicio(anyString())).thenReturn(0L);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicioMock);

        // Mocks para actualizarCalificacionProveedor
        when(proveedorRepository.findById(anyString())).thenReturn(Optional.of(proveedorMock));
        when(resenaRepository.findResenasByServicio("servicio-789")).thenReturn(new ArrayList<>());
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorMock);

        // Act
        MessageResponse resultado = resenaService.eliminarResena("resena-222", "cliente-123");

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getMessage().contains("eliminada exitosamente"));
        verify(resenaRepository).findById("resena-222");
        verify(resenaRepository).delete(resenaMock);
        verify(servicioRepository).save(any(Servicio.class));
        verify(resenaRepository).findResenasByServicio("servicio-789");
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Eliminar reseña - Reseña no encontrada")
    void eliminarResena_NoEncontrada() {
        // Arrange
        when(resenaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.eliminarResena("resena-inexistente", "cliente-123")
        );

        assertEquals("Reseña no encontrada con ID: resena-inexistente", exception.getMessage());
        verify(resenaRepository, never()).delete(any(Resena.class));
    }

    @Test
    @DisplayName("Eliminar reseña - No pertenece al cliente")
    void eliminarResena_NoPertenece() {
        // Arrange
        when(resenaRepository.findById(anyString())).thenReturn(Optional.of(resenaMock));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resenaService.eliminarResena("resena-222", "otro-cliente")
        );

        assertEquals("Solo el cliente dueño puede eliminar esta reseña", exception.getMessage());
        verify(resenaRepository, never()).delete(any(Resena.class));
    }

    // ==================== TESTS DE ACTUALIZAR CALIFICACIÓN ====================

    @Test
    @DisplayName("Actualizar calificación servicio - Exitoso")
    void actualizarCalificacionServicio_Exitoso() {
        // Arrange
        when(servicioRepository.findById(anyString())).thenReturn(Optional.of(servicioMock));
        when(resenaRepository.calcularPromedioCalificacion(anyString())).thenReturn(BigDecimal.valueOf(4.5));
        when(resenaRepository.contarResenasPorServicio(anyString())).thenReturn(10L);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicioMock);

        // Mocks para actualizarCalificacionProveedor - USA ID ESPECÍFICO
        when(proveedorRepository.findById(anyString())).thenReturn(Optional.of(proveedorMock));
        when(resenaRepository.findResenasByServicio("servicio-789")).thenReturn(List.of(resenaMock));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorMock);

        // Act
        resenaService.actualizarCalificacionServicio("servicio-789");

        // Assert
        verify(servicioRepository).findById("servicio-789");
        verify(resenaRepository).calcularPromedioCalificacion("servicio-789");
        verify(resenaRepository).contarResenasPorServicio("servicio-789");
        verify(servicioRepository).save(any(Servicio.class));
        verify(proveedorRepository).findById("proveedor-456");
        verify(resenaRepository).findResenasByServicio("servicio-789");
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Actualizar calificación servicio - Servicio no encontrado")
    void actualizarCalificacionServicio_NoEncontrado() {
        // Arrange
        when(servicioRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> resenaService.actualizarCalificacionServicio("servicio-inexistente")
        );

        assertEquals("Servicio no encontrado con ID: servicio-inexistente", exception.getMessage());
        verify(servicioRepository, never()).save(any(Servicio.class));
    }
}