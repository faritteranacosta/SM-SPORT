package com.sm_sport.service;

import com.sm_sport.dto.request.CambiarContrasenaRequest;
import com.sm_sport.dto.request.LoginRequest;
import com.sm_sport.dto.request.RecuperarContrasenaRequest;
import com.sm_sport.dto.request.RegistroUsuarioRequest;
import com.sm_sport.dto.response.AuthResponse;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.UnauthorizedException;
import com.sm_sport.mapper.UsuarioMapper;
import com.sm_sport.model.entity.Cliente;
import com.sm_sport.model.entity.Proveedor;
import com.sm_sport.model.enums.EstadoUsuario;
import com.sm_sport.model.enums.Rol;
import com.sm_sport.repository.ClienteRepository;
import com.sm_sport.repository.ProveedorRepository;
import com.sm_sport.repository.UsuarioRepository;
import com.sm_sport.security.JwtTokenProvider;
import com.sm_sport.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests Unitarios")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegistroUsuarioRequest registroClienteRequest;
    private RegistroUsuarioRequest registroProveedorRequest;
    private LoginRequest loginRequest;
    private CambiarContrasenaRequest cambiarContrasenaRequest;
    private RecuperarContrasenaRequest recuperarContrasenaRequest;
    private Cliente clienteMock;
    private Proveedor proveedorMock;

    @BeforeEach
    void setUp() {
        // Setup de datos de prueba
        setupRegistroClienteRequest();
        setupRegistroProveedorRequest();
        setupLoginRequest();
        setupCambiarContrasenaRequest();
        setupRecuperarContrasenaRequest();
        setupClienteMock();
        setupProveedorMock();
    }

    // ==================== TESTS DE REGISTRO ====================

    @Test
    @DisplayName("Registrar Cliente - Exitoso")
    void testRegistrarCliente_Exitoso() {
        // Arrange
        when(usuarioRepository.existsByCorreo(registroClienteRequest.getCorreo()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encrypted_password");
        when(usuarioMapper.toCliente(any(RegistroUsuarioRequest.class)))
                .thenReturn(clienteMock);
        when(clienteRepository.save(any(Cliente.class)))
                .thenReturn(clienteMock);
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyString()))
                .thenReturn("mock_jwt_token");

        // Act
        AuthResponse response = authService.registrar(registroClienteRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock_jwt_token");
        assertThat(response.getTipo()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400000L);
        assertThat(response.getIdUsuario()).isEqualTo(clienteMock.getIdUsuario());
        assertThat(response.getNombre()).isEqualTo(clienteMock.getNombre());
        assertThat(response.getCorreo()).isEqualTo(clienteMock.getCorreo());
        assertThat(response.getRol()).isEqualTo(Rol.CLIENTE);

        // Verify
        verify(usuarioRepository).existsByCorreo(registroClienteRequest.getCorreo());
        verify(passwordEncoder).encode(anyString());
        verify(usuarioMapper).toCliente(registroClienteRequest);
        verify(clienteRepository).save(any(Cliente.class));
        verify(jwtTokenProvider).generateToken(
                clienteMock.getCorreo(),
                clienteMock.getIdUsuario(),
                Rol.CLIENTE.name()
        );
    }

    @Test
    @DisplayName("Registrar Proveedor - Exitoso")
    void testRegistrarProveedor_Exitoso() {
        // Arrange
        when(usuarioRepository.existsByCorreo(registroProveedorRequest.getCorreo()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encrypted_password");
        when(usuarioMapper.toProveedor(any(RegistroUsuarioRequest.class)))
                .thenReturn(proveedorMock);
        when(proveedorRepository.save(any(Proveedor.class)))
                .thenReturn(proveedorMock);
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyString()))
                .thenReturn("mock_jwt_token");

        // Act
        AuthResponse response = authService.registrar(registroProveedorRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock_jwt_token");
        assertThat(response.getRol()).isEqualTo(Rol.PROVEEDOR);
        assertThat(response.getIdUsuario()).isEqualTo(proveedorMock.getIdUsuario());

        // Verify
        verify(usuarioRepository).existsByCorreo(registroProveedorRequest.getCorreo());
        verify(usuarioMapper).toProveedor(registroProveedorRequest);
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    @DisplayName("Registrar - Correo ya existe - Lanza BusinessException")
    void testRegistrar_CorreoExistente_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.existsByCorreo(registroClienteRequest.getCorreo()))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registrar(registroClienteRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("El correo electrónico ya está registrado");

        // Verify
        verify(usuarioRepository).existsByCorreo(registroClienteRequest.getCorreo());
        verify(clienteRepository, never()).save(any());
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar - Rol inválido - Lanza BusinessException")
    void testRegistrar_RolInvalido_DeberiaLanzarExcepcion() {
        // Arrange
        RegistroUsuarioRequest requestConRolAdmin = RegistroUsuarioRequest.builder()
                .nombre("Admin User")
                .correo("admin@test.com")
                .contrasena("Password123!")
                .rol(Rol.ADMINISTRADOR)
                .build();

        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted_password");

        // Act & Assert
        assertThatThrownBy(() -> authService.registrar(requestConRolAdmin))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Rol no válido para registro");

        // Verify
        verify(clienteRepository, never()).save(any());
        verify(proveedorRepository, never()).save(any());
    }

    // ==================== TESTS DE LOGIN ====================

    @Test
    @DisplayName("Login - Credenciales válidas - Retorna token")
    void testLogin_CredencialesValidas_RetornaToken() {
        // Arrange
        Authentication authenticationMock = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationMock);
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo()))
                .thenReturn(Optional.of(clienteMock));
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyString()))
                .thenReturn("mock_jwt_token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock_jwt_token");
        assertThat(response.getTipo()).isEqualTo("Bearer");
        assertThat(response.getIdUsuario()).isEqualTo(clienteMock.getIdUsuario());
        assertThat(response.getCorreo()).isEqualTo(clienteMock.getCorreo());
        assertThat(response.getRol()).isEqualTo(clienteMock.getRol());

        // Verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByCorreo(loginRequest.getCorreo());
        verify(jwtTokenProvider).generateToken(
                clienteMock.getCorreo(),
                clienteMock.getIdUsuario(),
                clienteMock.getRol().name()
        );
    }

    @Test
    @DisplayName("Login - Credenciales inválidas - Lanza UnauthorizedException")
    void testLogin_CredencialesInvalidas_LanzaExcepcion() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciales inválidas");

        // Verify
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Login - Usuario no encontrado - Lanza UnauthorizedException")
    void testLogin_UsuarioNoEncontrado_LanzaExcepcion() {
        // Arrange
        Authentication authenticationMock = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationMock);
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciales inválidas");

        // Verify
        verify(usuarioRepository).findByCorreo(loginRequest.getCorreo());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Login - Usuario inactivo - Lanza UnauthorizedException")
    void testLogin_UsuarioInactivo_LanzaExcepcion() {
        // Arrange
        clienteMock.setEstado(EstadoUsuario.INACTIVO);
        Authentication authenticationMock = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationMock);
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo()))
                .thenReturn(Optional.of(clienteMock));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuario inactivo o bloqueado");

        // Verify
        verify(usuarioRepository).findByCorreo(loginRequest.getCorreo());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString(), anyString());

        // Restaurar estado para otros tests
        clienteMock.setEstado(EstadoUsuario.ACTIVO);
    }

    @Test
    @DisplayName("Login - Usuario bloqueado - Lanza UnauthorizedException")
    void testLogin_UsuarioBloqueado_LanzaExcepcion() {
        // Arrange
        clienteMock.setEstado(EstadoUsuario.BLOQUEADO);
        Authentication authenticationMock = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationMock);
        when(usuarioRepository.findByCorreo(loginRequest.getCorreo()))
                .thenReturn(Optional.of(clienteMock));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Usuario inactivo o bloqueado");

        // Verify
        verify(usuarioRepository).findByCorreo(loginRequest.getCorreo());

        // Restaurar estado para otros tests
        clienteMock.setEstado(EstadoUsuario.ACTIVO);
    }

    // ==================== TESTS DE CAMBIAR CONTRASEÑA ====================

    @Test
    @DisplayName("Cambiar contraseña - Exitoso")
    void testCambiarContrasena_Exitoso() {
        // Arrange
        String idUsuario = "user123";

        when(usuarioRepository.findById(idUsuario))
                .thenReturn(Optional.of(clienteMock));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        when(passwordEncoder.encode(cambiarContrasenaRequest.getNuevaContrasena()))
                .thenReturn("new_encrypted_password");
        when(usuarioRepository.save(any(Cliente.class)))
                .thenReturn(clienteMock);

        // Act
        MessageResponse response = authService.cambiarContrasena(idUsuario, cambiarContrasenaRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Contraseña actualizada exitosamente");

        // Verify
        verify(usuarioRepository).findById(idUsuario);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(passwordEncoder).encode(cambiarContrasenaRequest.getNuevaContrasena());
        verify(usuarioRepository).save(clienteMock);
    }

    @Test
    @DisplayName("Cambiar contraseña - Usuario no encontrado - Lanza BusinessException")
    void testCambiarContrasena_UsuarioNoEncontrado_LanzaExcepcion() {
        // Arrange
        String idUsuario = "user_inexistente";

        when(usuarioRepository.findById(idUsuario))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.cambiarContrasena(idUsuario, cambiarContrasenaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Usuario no encontrado");

        // Verify
        verify(usuarioRepository).findById(idUsuario);
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cambiar contraseña - Contraseña actual incorrecta - Lanza BusinessException")
    void testCambiarContrasena_ContrasenaActualIncorrecta_LanzaExcepcion() {
        // Arrange
        String idUsuario = "user123";

        when(usuarioRepository.findById(idUsuario))
                .thenReturn(Optional.of(clienteMock));
        when(passwordEncoder.matches(
                cambiarContrasenaRequest.getContrasenaActual(),
                clienteMock.getContrasena()
        )).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.cambiarContrasena(idUsuario, cambiarContrasenaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("La contraseña actual es incorrecta");

        // Verify
        verify(usuarioRepository).findById(idUsuario);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(cambiarContrasenaRequest.getNuevaContrasena());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cambiar contraseña - Contraseñas no coinciden - Lanza BusinessException")
    void testCambiarContrasena_ContrasenasNoCoinciden_LanzaExcepcion() {
        // Arrange
        String idUsuario = "user123";
        CambiarContrasenaRequest requestInvalido = new CambiarContrasenaRequest();
        requestInvalido.setContrasenaActual("OldPassword123!");
        requestInvalido.setNuevaContrasena("NewPassword123!");
        requestInvalido.setConfirmarContrasena("DifferentPassword123!");

        // Act & Assert
        assertThatThrownBy(() -> authService.cambiarContrasena(idUsuario, requestInvalido))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Las contraseñas no coinciden");

        // Verify
        verify(usuarioRepository, never()).findById(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    // ==================== TESTS DE RECUPERAR CONTRASEÑA ====================

    @Test
    @DisplayName("Recuperar contraseña - Exitoso")
    void testRecuperarContrasena_Exitoso() {
        // Arrange
        when(usuarioRepository.findByCorreo(recuperarContrasenaRequest.getCorreo()))
                .thenReturn(Optional.of(clienteMock));

        // Act
        MessageResponse response = authService.recuperarContrasena(recuperarContrasenaRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage())
                .isEqualTo("Se ha enviado un correo con instrucciones para recuperar tu contraseña");

        // Verify
        verify(usuarioRepository).findByCorreo(recuperarContrasenaRequest.getCorreo());
    }

    @Test
    @DisplayName("Recuperar contraseña - Usuario no encontrado - Lanza BusinessException")
    void testRecuperarContrasena_UsuarioNoEncontrado_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findByCorreo(recuperarContrasenaRequest.getCorreo()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.recuperarContrasena(recuperarContrasenaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Usuario no encontrado");

        // Verify
        verify(usuarioRepository).findByCorreo(recuperarContrasenaRequest.getCorreo());
    }

    // ==================== TESTS DE VALIDAR TOKEN ====================

    @Test
    @DisplayName("Validar token - Token válido - Retorna true")
    void testValidarToken_TokenValido_RetornaTrue() {
        // Arrange
        String tokenValido = "valid_jwt_token";
        when(jwtTokenProvider.validateToken(tokenValido))
                .thenReturn(true);

        // Act
        boolean resultado = authService.validarToken(tokenValido);

        // Assert
        assertThat(resultado).isTrue();

        // Verify
        verify(jwtTokenProvider).validateToken(tokenValido);
    }

    @Test
    @DisplayName("Validar token - Token inválido - Retorna false")
    void testValidarToken_TokenInvalido_RetornaFalse() {
        // Arrange
        String tokenInvalido = "invalid_jwt_token";
        when(jwtTokenProvider.validateToken(tokenInvalido))
                .thenReturn(false);

        // Act
        boolean resultado = authService.validarToken(tokenInvalido);

        // Assert
        assertThat(resultado).isFalse();

        // Verify
        verify(jwtTokenProvider).validateToken(tokenInvalido);
    }

    @Test
    @DisplayName("Validar token - Excepción en validación - Retorna false")
    void testValidarToken_ExcepcionEnValidacion_RetornaFalse() {
        // Arrange
        String token = "token_con_error";
        when(jwtTokenProvider.validateToken(token))
                .thenThrow(new RuntimeException("Token validation error"));

        // Act
        boolean resultado = authService.validarToken(token);

        // Assert
        assertThat(resultado).isFalse();

        // Verify
        verify(jwtTokenProvider).validateToken(token);
    }

    // ==================== TESTS DE REFRESCAR TOKEN ====================

    @Test
    @DisplayName("Refrescar token - No implementado - Lanza UnsupportedOperationException")
    void testRefrescarToken_NoImplementado_LanzaExcepcion() {
        // Arrange
        String refreshToken = "refresh_token";

        // Act & Assert
        assertThatThrownBy(() -> authService.refrescarToken(refreshToken))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Funcionalidad pendiente de implementación");
    }

    // ==================== MÉTODOS DE SETUP ====================

    private void setupRegistroClienteRequest() {
        registroClienteRequest = RegistroUsuarioRequest.builder()
                .nombre("Juan Pérez")
                .correo("juan.perez@test.com")
                .contrasena("Password123!")
                .telefono("3001234567")
                .direccion("Calle 123 #45-67")
                .rol(Rol.CLIENTE)
                .preferenciaDeportes("Fútbol, Baloncesto")
                .nivelExperiencia("Intermedio")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .build();
    }

    private void setupRegistroProveedorRequest() {
        registroProveedorRequest = RegistroUsuarioRequest.builder()
                .nombre("Deportes XYZ")
                .correo("contacto@deportesxyz.com")
                .contrasena("Password123!")
                .telefono("3007654321")
                .direccion("Avenida Principal #10-20")
                .rol(Rol.PROVEEDOR)
                .descripcionNegocio("Proveedor de canchas deportivas en Santa Marta")
                .build();
    }

    private void setupLoginRequest() {
        loginRequest = LoginRequest.builder()
                .correo("juan.perez@test.com")
                .contrasena("Password123!")
                .build();
    }

    private void setupCambiarContrasenaRequest() {
        cambiarContrasenaRequest = new CambiarContrasenaRequest();
        cambiarContrasenaRequest.setContrasenaActual("Password123!");
        cambiarContrasenaRequest.setNuevaContrasena("NewPassword456!");
        cambiarContrasenaRequest.setConfirmarContrasena("NewPassword456!");
    }

    private void setupRecuperarContrasenaRequest() {
        recuperarContrasenaRequest = new RecuperarContrasenaRequest();
        recuperarContrasenaRequest.setCorreo("juan.perez@test.com");
    }

    private void setupClienteMock() {
        clienteMock = new Cliente();
        clienteMock.setIdUsuario("cliente123");
        clienteMock.setNombre("Juan Pérez");
        clienteMock.setCorreo("juan.perez@test.com");
        clienteMock.setContrasena("encrypted_password");
        clienteMock.setTelefono("3001234567");
        clienteMock.setRol(Rol.CLIENTE);
        clienteMock.setEstado(EstadoUsuario.ACTIVO);
    }

    private void setupProveedorMock() {
        proveedorMock = new Proveedor();
        proveedorMock.setIdUsuario("proveedor123");
        proveedorMock.setNombre("Deportes XYZ");
        proveedorMock.setCorreo("contacto@deportesxyz.com");
        proveedorMock.setContrasena("encrypted_password");
        proveedorMock.setTelefono("3007654321");
        proveedorMock.setRol(Rol.PROVEEDOR);
        proveedorMock.setEstado(EstadoUsuario.ACTIVO);
    }
}
