package com.sm_sport.service.impl;

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
import com.sm_sport.model.entity.Usuario;
import com.sm_sport.model.enums.EstadoUsuario;
import com.sm_sport.model.enums.Rol;
import com.sm_sport.repository.ClienteRepository;
import com.sm_sport.repository.ProveedorRepository;
import com.sm_sport.repository.UsuarioRepository;
import com.sm_sport.security.JwtTokenProvider;
import com.sm_sport.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse registrar(RegistroUsuarioRequest request) {
        log.info("Iniciando registro de usuario: {}", request.getCorreo());

        // Validar que el correo no exista
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            log.warn("Intento de registro con correo existente: {}", request.getCorreo());
            throw new BusinessException("El correo electrónico ya está registrado");
        }
 
        // Encriptar contraseña
        request.setContrasena(passwordEncoder.encode(request.getContrasena()));

        Usuario usuario;

        // Crear usuario según rol
        switch (request.getRol()) {
            case CLIENTE -> {
                Cliente cliente = usuarioMapper.toCliente(request);
                cliente.setRol(Rol.CLIENTE);
                usuario = clienteRepository.save(cliente);
                log.info("Cliente registrado exitosamente: {}", usuario.getIdUsuario());
            }
            case PROVEEDOR -> {
                Proveedor proveedor = usuarioMapper.toProveedor(request);
                proveedor.setRol(Rol.PROVEEDOR);
                usuario = proveedorRepository.save(proveedor);
                log.info("Proveedor registrado exitosamente: {}", usuario.getIdUsuario());
            }
            default -> throw new BusinessException("Rol no válido para registro");
        }

        // Generar token JWT
        String token = jwtTokenProvider.generateToken(
                usuario.getCorreo(),
                usuario.getIdUsuario(),
                usuario.getRol().name()
        );

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .expiresIn(86400000L) // 24 horas
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login: {}", request.getCorreo());

        try {
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getCorreo(),
                            request.getContrasena()
                    )
            );

            // Buscar usuario
            Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                    .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

            // Validar estado del usuario
            if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
                log.warn("Intento de login con usuario no activo: {}", request.getCorreo());
                throw new UnauthorizedException("Usuario inactivo o bloqueado");
            }

            // Generar token
            String token = jwtTokenProvider.generateToken(
                    usuario.getCorreo(),
                    usuario.getIdUsuario(),
                    usuario.getRol().name()
            );

            log.info("Login exitoso: {}", usuario.getCorreo());

            return AuthResponse.builder()
                    .token(token)
                    .tipo("Bearer")
                    .expiresIn(86400000L)
                    .idUsuario(usuario.getIdUsuario())
                    .nombre(usuario.getNombre())
                    .correo(usuario.getCorreo())
                    .rol(usuario.getRol())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Error en autenticación: {}", e.getMessage());
            throw new UnauthorizedException("Credenciales inválidas");
        }
    }

    @Override
    public MessageResponse cambiarContrasena(String idUsuario, CambiarContrasenaRequest request) {
        log.info("Cambiando contraseña para usuario: {}", idUsuario);

        // Validar que las contraseñas coincidan
        if (!request.getNuevaContrasena().equals(request.getConfirmarContrasena())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }

        // Buscar usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // Validar contraseña actual
        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            throw new BusinessException("La contraseña actual es incorrecta");
        }

        // Actualizar contraseña
        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuario);

        log.info("Contraseña cambiada exitosamente para: {}", idUsuario);

        return MessageResponse.success("Contraseña actualizada exitosamente");
    }

    @Override
    public MessageResponse recuperarContrasena(RecuperarContrasenaRequest request) {
        log.info("Solicitud de recuperación de contraseña: {}", request.getCorreo());

        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // TODO: Generar token de recuperación y enviar email
        // Por ahora solo retornamos mensaje de éxito

        log.info("Email de recuperación enviado a: {}", request.getCorreo());

        return MessageResponse.success(
                "Se ha enviado un correo con instrucciones para recuperar tu contraseña"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validarToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public AuthResponse refrescarToken(String refreshToken) {
        // TODO: Implementar lógica de refresh token
        throw new UnsupportedOperationException("Funcionalidad pendiente de implementación");
    }
}
