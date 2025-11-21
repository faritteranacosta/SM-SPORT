package com.sm_sport.service;

import com.sm_sport.dto.request.CrearServicioRequest;
import com.sm_sport.dto.request.DisponibilidadRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.ServicioResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.DisponibilidadMapper;
import com.sm_sport.mapper.PageMapper;
import com.sm_sport.mapper.ServicioMapper;
import com.sm_sport.mapper.UbicacionMapper;
import com.sm_sport.model.entity.DisponibilidadServicio;
import com.sm_sport.model.entity.Proveedor;
import com.sm_sport.model.entity.Reserva;
import com.sm_sport.model.entity.Servicio;
import com.sm_sport.model.entity.UbicacionServicio;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.model.enums.EstadoServicio;
import com.sm_sport.repository.DisponibilidadServicioRepository;
import com.sm_sport.repository.ProveedorRepository;
import com.sm_sport.repository.ServicioRepository;
import com.sm_sport.repository.UbicacionServicioRepository;
import com.sm_sport.service.impl.ServicioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ServicioServiceImpl.
 * Nombre de la clase pedido por el usuario: ServicioServicieTest
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ServicioServiceTest {

    @InjectMocks
    private ServicioServiceImpl servicioService;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private UbicacionServicioRepository ubicacionRepository;

    @Mock
    private DisponibilidadServicioRepository disponibilidadRepository;

    @Mock
    private ServicioMapper servicioMapper;

    @Mock
    private UbicacionMapper ubicacionMapper;

    @Mock
    private DisponibilidadMapper disponibilidadMapper;

    @Mock
    private PageMapper pageMapper;

    // objetos reutilizables en pruebas
    private Proveedor proveedor;
    private Servicio servicio;
    private CrearServicioRequest crearRequest;

    @BeforeEach
    void setup() {
        // crear proveedor base
        proveedor = new Proveedor();
        proveedor.setIdUsuario("prov-1");
        proveedor.setSaldoCuenta(new BigDecimal("100.00"));
        proveedor.setTotalServiciosPublicados(0);


        // crear servicio base (sin id hasta que el repo lo "guarde")
        servicio = Servicio.builder()
                .idServicio("svc-1")
                .nombre("Cancha de Fútbol 5")
                .deporte("Fútbol")
                .precio(new BigDecimal("50000.00"))
                .estado(EstadoServicio.PUBLICADO)
                .proveedor(proveedor)
                .build();

        // request mínimo válido (se pueden completar más campos si se desea)
        crearRequest = CrearServicioRequest.builder()
                .nombre("Cancha de Prueba")
                .deporte("Fútbol")
                .precio(new BigDecimal("50000.00"))
                .ubicacion(null) // en algunos tests se proveerá ubicación; aquí la dejamos null
                .build();
    }

    @Test
    void publicarServicio_success() {
        // Arrange
        // proveedor existe y tiene saldo >= 0
        when(proveedorRepository.findById("prov-1")).thenReturn(Optional.of(proveedor));

        // mapper convierte request a entidad (sin id)
        Servicio toSave = Servicio.builder()
                .nombre(crearRequest.getNombre())
                .deporte(crearRequest.getDeporte())
                .precio(crearRequest.getPrecio())
                .build();
        when(servicioMapper.toEntity(crearRequest)).thenReturn(toSave);

        // repo guarda y devuelve entidad con id asignado
        Servicio saved = Servicio.builder()
                .idServicio("svc-123")
                .nombre(toSave.getNombre())
                .deporte(toSave.getDeporte())
                .precio(toSave.getPrecio())
                .proveedor(proveedor)
                .estado(EstadoServicio.PUBLICADO)
                .build();
        when(servicioRepository.save(any(Servicio.class))).thenReturn(saved);

        // mapper a response
        ServicioResponse resp = ServicioResponse.builder()
                .idServicio(saved.getIdServicio())
                .nombre(saved.getNombre())
                .deporte(saved.getDeporte())
                .precio(saved.getPrecio())
                .estado(saved.getEstado())
                .idProveedor(proveedor.getIdUsuario())
                .build();
        when(servicioMapper.toResponse(saved)).thenReturn(resp);

        // Act
        ServicioResponse resultado = servicioService.publicarServicio("prov-1", crearRequest);

        // Assert
        assertNotNull(resultado);
        assertEquals("svc-123", resultado.getIdServicio());
        assertEquals("Cancha de Prueba", resultado.getNombre());
        verify(proveedorRepository).findById("prov-1");
        verify(servicioRepository).save(any(Servicio.class));
        verify(proveedorRepository).save(proveedor);
    }

    @Test
    void publicarServicio_proveedorNoEncontrado_throwsResourceNotFound() {
        // Arrange
        when(proveedorRepository.findById("prov-1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> servicioService.publicarServicio("prov-1", crearRequest));
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void publicarServicio_saldoNegativo_throwsBusinessException() {
        // Arrange
        proveedor.setSaldoCuenta(new BigDecimal("-10.00"));
        when(proveedorRepository.findById("prov-1")).thenReturn(Optional.of(proveedor));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> servicioService.publicarServicio("prov-1", crearRequest));
        assertTrue(ex.getMessage().toLowerCase().contains("recargar"));
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_success() {
        // Arrange
        when(servicioRepository.findById("svc-1")).thenReturn(Optional.of(servicio));
        ServicioResponse resp = ServicioResponse.builder()
                .idServicio("svc-1")
                .nombre(servicio.getNombre())
                .build();
        when(servicioMapper.toResponse(servicio)).thenReturn(resp);

        // Act
        ServicioResponse resultado = servicioService.obtenerPorId("svc-1");

        // Assert
        assertNotNull(resultado);
        assertEquals("svc-1", resultado.getIdServicio());
        verify(servicioRepository).findById("svc-1");
    }

    @Test
    void obtenerPorId_noExiste_throwsResourceNotFound() {
        when(servicioRepository.findById("no-existe")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> servicioService.obtenerPorId("no-existe"));
        verify(servicioRepository).findById("no-existe");
    }

    @Test
    void eliminarServicio_conReservasActivas_throwsBusinessException() {
        // Arrange
        Servicio svc = Servicio.builder().idServicio("svc-2").estado(EstadoServicio.PUBLICADO).build();
        Reserva reservaMock = mock(Reserva.class);
        when(reservaMock.getEstado()).thenReturn(EstadoReserva.PENDIENTE);
        svc.setReservas(Collections.singletonList(reservaMock));

        when(servicioRepository.findById("svc-2")).thenReturn(Optional.of(svc));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> servicioService.eliminarServicio("svc-2"));
        assertTrue(ex.getMessage().toLowerCase().contains("reservas activas"));
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void eliminarServicio_success_softDelete() {
        // Arrange
        Servicio svc = Servicio.builder().idServicio("svc-3").estado(EstadoServicio.PUBLICADO).build();
        svc.setReservas(Collections.emptyList());
        when(servicioRepository.findById("svc-3")).thenReturn(Optional.of(svc));
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MessageResponse resp = servicioService.eliminarServicio("svc-3");

        // Assert
        assertNotNull(resp);
        assertEquals("Servicio eliminado exitosamente", resp.getMessage());
        assertEquals(EstadoServicio.ELIMINADO, svc.getEstado());
        verify(servicioRepository).save(svc);
    }

    @Test
    void agregarDisponibilidad_success() {
        // Arrange
        Servicio svc = Servicio.builder().idServicio("svc-4").build();
        when(servicioRepository.findById("svc-4")).thenReturn(Optional.of(svc));

        DisponibilidadRequest dReq = new DisponibilidadRequest(); // campos no relevantes para esta prueba
        List<DisponibilidadRequest> reqs = List.of(dReq);

        DisponibilidadServicio dEntity = new DisponibilidadServicio();
        when(disponibilidadMapper.toEntityList(reqs)).thenReturn(List.of(dEntity));
        when(disponibilidadRepository.saveAll(anyList())).thenReturn(List.of(dEntity));

        // Act
        MessageResponse resp = servicioService.agregarDisponibilidad("svc-4", reqs);

        // Assert
        assertNotNull(resp);
        assertEquals("Disponibilidad agregada exitosamente", resp.getMessage());
        verify(disponibilidadRepository).saveAll(anyList());
    }

    @Test
    void cambiarEstado_success() {
        // Arrange
        Servicio svc = Servicio.builder().idServicio("svc-5").estado(EstadoServicio.PUBLICADO).build();
        when(servicioRepository.findById("svc-5")).thenReturn(Optional.of(svc));
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicioResponse respDto = ServicioResponse.builder()
                .idServicio(svc.getIdServicio())
                .estado(EstadoServicio.PAUSADO)
                .build();
        when(servicioMapper.toResponse(any(Servicio.class))).thenReturn(respDto);

        // Act
        ServicioResponse resultado = servicioService.cambiarEstado("svc-5", EstadoServicio.PAUSADO);

        // Assert
        assertNotNull(resultado);
        assertEquals(EstadoServicio.PAUSADO, resultado.getEstado());
        verify(servicioRepository).save(any(Servicio.class));
    }

    @Test
    void listarServicios_invocaRepositorio() {
        // Arrange
        Page<Servicio> pageMock = mock(Page.class);
        when(servicioRepository.findByEstado(eq(EstadoServicio.PUBLICADO), any(Pageable.class))).thenReturn(pageMock);

        // el mapper de pagina (pageMapper) será llamado dentro del método; mockear su respuesta
        when(pageMapper.toPageResponse(any())).thenReturn(null); // no necesitamos el valor real para verificar interacción

        // Act
        servicioService.listarServicios(0, 10);

        // Assert
        verify(servicioRepository).findByEstado(eq(EstadoServicio.PUBLICADO), any(Pageable.class));
        verify(pageMapper).toPageResponse(any());
    }
}
