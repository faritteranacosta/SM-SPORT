package com.sm_sport.service;

import com.sm_sport.dto.request.ActualizarEstadoUsuarioRequest;
import com.sm_sport.dto.request.ActualizarPerfilRequest;
import com.sm_sport.dto.request.FiltroUsuarioRequest;
import com.sm_sport.dto.response.*;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.UsuarioMapper;
import com.sm_sport.model.entity.Cliente;
import com.sm_sport.model.entity.Proveedor;
import com.sm_sport.model.entity.Usuario;
import com.sm_sport.model.enums.EstadoUsuario;
import com.sm_sport.model.enums.Rol;
import com.sm_sport.repository.ClienteRepository;
import com.sm_sport.repository.ProveedorRepository;
import com.sm_sport.repository.UsuarioRepository;
import com.sm_sport.service.impl.UsuarioServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Tests Unitarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioMock;
    private UsuarioResponse usuarioResponseMock;

    private Cliente clienteMock;
    private Proveedor proveedorMock;

    @BeforeEach
    void setUp() {
        // [CORRECCIÓN DE CLASE ABSTRACTA] Se utiliza Mockito.mock()
        usuarioMock = mock(Usuario.class);

        // Inicializamos los mocks de subclases como concretos
        clienteMock = new Cliente();
        proveedorMock = new Proveedor();

        // **Configuración de los valores para el mock de Usuario:**
        // [CORRECCIÓN FINAL: UnnecessaryStubbingException]
        // Se usa lenient() para indicar a Mockito que estos stubs son opcionales
        // y evitar la excepción cuando no son llamados por todos los tests.
        lenient().when(usuarioMock.getIdUsuario()).thenReturn("user123");
        lenient().when(usuarioMock.getNombre()).thenReturn("Carlos Díaz");
        lenient().when(usuarioMock.getCorreo()).thenReturn("carlos@test.com");
        lenient().when(usuarioMock.getTelefono()).thenReturn("123456789");
        lenient().when(usuarioMock.getDireccion()).thenReturn("Calle 1 # 2-3");
        lenient().when(usuarioMock.getEstado()).thenReturn(EstadoUsuario.ACTIVO);
        lenient().when(usuarioMock.getRol()).thenReturn(Rol.CLIENTE); // Rol por defecto
        lenient().when(usuarioMock.getFechaRegistro()).thenReturn(LocalDateTime.now());
        lenient().when(usuarioMock.getFechaActualizacion()).thenReturn(LocalDateTime.now());
        lenient().when(usuarioMock.getContrasena()).thenReturn("hashed123");
    }

    // =============================================================
    // obtenerPorId()
    // =============================================================

    @Test
    @DisplayName("obtenerPorId - Exitoso")
    void testObtenerPorId_Exitoso() {
        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(usuarioMock));

        UsuarioResponse response = UsuarioResponse.builder()
                .idUsuario("user123")
                .nombre("Carlos Díaz")
                .correo("carlos@test.com")
                .build();

        when(usuarioMapper.toResponse(usuarioMock)).thenReturn(response);

        UsuarioResponse result = usuarioService.obtenerPorId("user123");

        assertThat(result).isNotNull();
        assertThat(result.getIdUsuario()).isEqualTo("user123");
        verify(usuarioRepository).findById("user123");
        verify(usuarioMapper).toResponse(usuarioMock);
    }

    @Test
    @DisplayName("obtenerPorId - Usuario no encontrado lanza excepción")
    void testObtenerPorId_NoEncontrado() {
        when(usuarioRepository.findById("user123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtenerPorId("user123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(usuarioRepository).findById("user123");
    }

    // =============================================================
    // obtenerPerfil()
    // =============================================================

    @Test
    @DisplayName("obtenerPerfil - Perfil CLIENTE exitoso")
    void testObtenerPerfil_Cliente() {
        clienteMock.setIdUsuario("user123");
        clienteMock.setRol(Rol.CLIENTE);
        clienteMock.setNombre("Carlos Cliente");

        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(clienteMock));
        when(clienteRepository.findById("user123")).thenReturn(Optional.of(clienteMock));

        when(usuarioMapper.toClienteResponse(clienteMock))
                .thenReturn(ClienteResponse.builder()
                        .idUsuario("user123")
                        .nombre("Carlos Cliente")
                        .build());

        UsuarioResponse result = usuarioService.obtenerPerfil("user123");
        assertThat(result).isNotNull();

        verify(clienteRepository).findById("user123");
    }

    @Test
    @DisplayName("obtenerPerfil - Perfil PROVEEDOR exitoso")
    void testObtenerPerfil_Proveedor() {
        proveedorMock.setIdUsuario("user123");
        proveedorMock.setRol(Rol.PROVEEDOR);
        proveedorMock.setNombre("Proveedor Pro");

        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(proveedorMock));
        when(proveedorRepository.findById("user123")).thenReturn(Optional.of(proveedorMock));

        when(usuarioMapper.toProveedorResponse(proveedorMock))
                .thenReturn(ProveedorResponse.builder()
                        .idUsuario("user123")
                        .nombre("Proveedor Pro")
                        .calificacionPromedio(BigDecimal.ZERO)
                        .build());

        UsuarioResponse result = usuarioService.obtenerPerfil("user123");

        assertThat(result).isNotNull();
        verify(proveedorRepository).findById("user123");
    }

    // =============================================================
    // actualizarPerfil()
    // =============================================================

    @Test
    @DisplayName("actualizarPerfil - Exitoso")
    void testActualizarPerfil_Exitoso() {
        clienteMock.setIdUsuario("user123");
        clienteMock.setRol(Rol.CLIENTE);

        ActualizarPerfilRequest request = new ActualizarPerfilRequest();
        request.setNombre("Nuevo Nombre");
        request.setTelefono("555999");

        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(clienteMock));
        doNothing().when(usuarioMapper).updateEntityFromRequest(request, clienteMock);

        when(usuarioRepository.save(clienteMock)).thenReturn(clienteMock);

        UsuarioResponse mapped = UsuarioResponse.builder()
                .idUsuario("user123")
                .nombre("Nuevo Nombre")
                .build();

        when(usuarioMapper.toResponse(clienteMock)).thenReturn(mapped);

        UsuarioResponse result = usuarioService.actualizarPerfil("user123", request);

        assertThat(result.getNombre()).isEqualTo("Nuevo Nombre");
        verify(usuarioRepository).save(clienteMock);
    }

    @Test
    @DisplayName("actualizarPerfil - Usuario inactivo lanza excepción")
    void testActualizarPerfil_Inactivo() {
        // Configuración específica para este test: el estado debe ser INACTIVO
        when(usuarioMock.getEstado()).thenReturn(EstadoUsuario.INACTIVO);

        ActualizarPerfilRequest req = new ActualizarPerfilRequest();

        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(usuarioMock));

        assertThatThrownBy(() -> usuarioService.actualizarPerfil("user123", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactivo");

        verify(usuarioRepository).findById("user123");
    }

    // =============================================================
    // listarUsuarios()
    // =============================================================

    @Test
    @DisplayName("listarUsuarios - Exitoso con paginación")
    void testListarUsuarios() {
        FiltroUsuarioRequest filtros = new FiltroUsuarioRequest();
        filtros.setPagina(0);
        filtros.setTamano(10);
        filtros.setDireccion("ASC");
        filtros.setOrdenarPor("nombre");

        List<Usuario> usuarios = List.of(usuarioMock);

        Page<Usuario> page = new PageImpl<>(usuarios);

        when(usuarioRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        when(usuarioMapper.toResponseList(usuarios))
                .thenReturn(List.of(
                        UsuarioResponse.builder()
                                .idUsuario("user123")
                                .nombre("Carlos")
                                .build()
                ));

        PageResponse<UsuarioResponse> result = usuarioService.listarUsuarios(filtros);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(usuarioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // =============================================================
    // cambiarEstado()
    // =============================================================

    @Test
    @DisplayName("cambiarEstado - Exitoso")
    void testCambiarEstado_Exitoso() {
        ActualizarEstadoUsuarioRequest request = new ActualizarEstadoUsuarioRequest();
        request.setEstado(EstadoUsuario.SUSPENDIDO);
        request.setMotivo("Prueba de suspensión");

        // El estado inicial es ACTIVO (configurado en setUp, pero lo reaseguramos si fuese necesario)
        when(usuarioMock.getEstado()).thenReturn(EstadoUsuario.ACTIVO);
        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(usuarioMock));
        when(usuarioRepository.save(usuarioMock)).thenReturn(usuarioMock);

        MessageResponse response = usuarioService.cambiarEstado("user123", request);

        assertThat(response.getMessage()).contains("SUSPENDIDO");
        verify(usuarioRepository).save(usuarioMock);
    }

    @Test
    @DisplayName("cambiarEstado - Estado igual lanza excepción")
    void testCambiarEstado_Igual() {
        ActualizarEstadoUsuarioRequest req = new ActualizarEstadoUsuarioRequest();
        req.setEstado(EstadoUsuario.ACTIVO);

        // El estado actual es ACTIVO
        when(usuarioMock.getEstado()).thenReturn(EstadoUsuario.ACTIVO);
        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(usuarioMock));

        assertThatThrownBy(() -> usuarioService.cambiarEstado("user123", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya tiene el estado");

        verify(usuarioRepository).findById("user123");
    }

    // =============================================================
    // eliminarUsuario()
    // =============================================================

    @Test
    @DisplayName("eliminarUsuario - Exitoso")
    void testEliminarUsuario_Exitoso() {
        // El estado inicial es ACTIVO
        when(usuarioMock.getEstado()).thenReturn(EstadoUsuario.ACTIVO);

        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(usuarioMock));
        when(usuarioRepository.save(usuarioMock)).thenReturn(usuarioMock);

        MessageResponse response = usuarioService.eliminarUsuario("user123");

        assertThat(response.getMessage()).contains("exitosamente");
        verify(usuarioRepository).save(usuarioMock);
    }

    @Test
    @DisplayName("eliminarUsuario - Ya inactivo lanza excepción")
    void testEliminarUsuario_Inactivo() {
        // El estado actual es INACTIVO
        when(usuarioMock.getEstado()).thenReturn(EstadoUsuario.INACTIVO);

        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(usuarioMock));

        assertThatThrownBy(() -> usuarioService.eliminarUsuario("user123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya está inactivo");

        verify(usuarioRepository).findById("user123");
    }

    // =============================================================
    // obtenerCliente()
    // =============================================================

    @Test
    @DisplayName("obtenerCliente - Exitoso")
    void testObtenerCliente() {
        clienteMock.setIdUsuario("client999");

        when(clienteRepository.findById("client999")).thenReturn(Optional.of(clienteMock));

        ClienteResponse responseMock = ClienteResponse.builder()
                .idUsuario("client999")
                .nombre("Pepito")
                .build();

        when(usuarioMapper.toClienteResponse(clienteMock)).thenReturn(responseMock);

        ClienteResponse response = usuarioService.obtenerCliente("client999");

        assertThat(response.getIdUsuario()).isEqualTo("client999");
        verify(clienteRepository).findById("client999");
    }

    // =============================================================
    // obtenerProveedor()
    // =============================================================

    @Test
    @DisplayName("obtenerProveedor - Exitoso")
    void testObtenerProveedor() {
        proveedorMock.setIdUsuario("prov777");

        when(proveedorRepository.findById("prov777")).thenReturn(Optional.of(proveedorMock));

        ProveedorResponse responseMock = ProveedorResponse.builder()
                .idUsuario("prov777")
                .nombre("Proveedor Test")
                .calificacionPromedio(BigDecimal.valueOf(4.5))
                .build();

        when(usuarioMapper.toProveedorResponse(proveedorMock)).thenReturn(responseMock);

        ProveedorResponse response = usuarioService.obtenerProveedor("prov777");

        assertThat(response.getIdUsuario()).isEqualTo("prov777");
        verify(proveedorRepository).findById("prov777");
    }

    // =============================================================
    // existeCorreo()
    // =============================================================

    @Test
    @DisplayName("existeCorreo - Exitoso")
    void testExisteCorreo() {
        when(usuarioRepository.existsByCorreo("test@mail.com")).thenReturn(true);

        boolean result = usuarioService.existeCorreo("test@mail.com");

        assertThat(result).isTrue();
        verify(usuarioRepository).existsByCorreo("test@mail.com");
    }
}