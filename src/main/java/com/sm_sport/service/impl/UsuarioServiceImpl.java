package com.sm_sport.service.impl;

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
import com.sm_sport.repository.ClienteRepository;
import com.sm_sport.repository.ProveedorRepository;
import com.sm_sport.repository.UsuarioRepository;
import com.sm_sport.service.UsuarioService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(String idUsuario) {
        log.info("Obteniendo usuario con ID: {}", idUsuario);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(String idUsuario) {
        log.info("Obteniendo perfil del usuario: {}", idUsuario);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        // Retornar respuesta específica según el rol
        return switch (usuario.getRol()) {
            case CLIENTE -> {
                Cliente cliente = clienteRepository.findById(idUsuario)
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
                yield usuarioMapper.toClienteResponse(cliente);
            }
            case PROVEEDOR -> {
                Proveedor proveedor = proveedorRepository.findById(idUsuario)
                        .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
                yield usuarioMapper.toProveedorResponse(proveedor);
            }
            default -> usuarioMapper.toResponse(usuario);
        };
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarPerfil(String idUsuario, ActualizarPerfilRequest request) {
        log.info("Actualizando perfil del usuario: {}", idUsuario);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        // Validar que el usuario esté activo
        if (usuario.getEstado() == EstadoUsuario.INACTIVO || usuario.getEstado() == EstadoUsuario.SUSPENDIDO) {
            throw new BusinessException("No se puede actualizar el perfil de un usuario inactivo o suspendido");
        }

        // Actualizar campos comunes
        usuarioMapper.updateEntityFromRequest(request, usuario);

        // Actualizar campos específicos según el rol
        switch (usuario.getRol()) {
            case CLIENTE -> {
                Cliente cliente = (Cliente) usuario;
                if (request.getPreferenciaDeportes() != null) {
                    cliente.setPreferenciaDeportes(request.getPreferenciaDeportes());
                }
                if (request.getNivelExperiencia() != null) {
                    cliente.setNivelExperiencia(request.getNivelExperiencia());
                }
            }
            case PROVEEDOR -> {
                Proveedor proveedor = (Proveedor) usuario;
                if (request.getDescripcionNegocio() != null) {
                    proveedor.setDescripcionNegocio(request.getDescripcionNegocio());
                }
            }
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        log.info("Perfil actualizado exitosamente para el usuario: {}", idUsuario);

        return usuarioMapper.toResponse(usuarioActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listarUsuarios(FiltroUsuarioRequest filtros) {
        log.info("Listando usuarios con filtros: {}", filtros);

        // Crear specification para filtros dinámicos
        Specification<Usuario> specification = crearSpecification(filtros);

        // Configurar paginación y ordenamiento
        Sort sort = Sort.by(
                filtros.getDireccion().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                filtros.getOrdenarPor()
        );

        Pageable pageable = PageRequest.of(filtros.getPagina(), filtros.getTamano(), sort);

        // Obtener página de usuarios
        Page<Usuario> page = usuarioRepository.findAll(specification, pageable);

        // Mapear a response
        List<UsuarioResponse> usuarios = usuarioMapper.toResponseList(page.getContent());

        log.info("Se encontraron {} usuarios", page.getTotalElements());

        return PageResponse.<UsuarioResponse>builder()
                .content(usuarios)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public MessageResponse cambiarEstado(String idUsuario, ActualizarEstadoUsuarioRequest request) {
        log.info("Cambiando estado del usuario {} a {}", idUsuario, request.getEstado());

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        // Validar que no sea el mismo estado
        if (usuario.getEstado() == request.getEstado()) {
            throw new BusinessException("El usuario ya tiene el estado: " + request.getEstado());
        }

        // Cambiar estado
        EstadoUsuario estadoAnterior = usuario.getEstado();
        usuario.setEstado(request.getEstado());

        usuarioRepository.save(usuario);

        String mensaje = String.format("Estado del usuario cambiado de %s a %s",
                estadoAnterior, request.getEstado());

        if (request.getMotivo() != null && !request.getMotivo().isEmpty()) {
            mensaje += ". Motivo: " + request.getMotivo();
        }

        log.info("Estado actualizado exitosamente para el usuario: {}", idUsuario);

        return MessageResponse.success(mensaje);
    }

    @Override
    @Transactional
    public MessageResponse eliminarUsuario(String idUsuario) {
        log.info("Eliminando usuario (soft delete): {}", idUsuario);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        // Validar que no esté ya inactivo
        if (usuario.getEstado() == EstadoUsuario.INACTIVO) {
            throw new BusinessException("El usuario ya está inactivo");
        }

        // Soft delete: cambiar estado a INACTIVO
        usuario.setEstado(EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);

        log.info("Usuario {} marcado como INACTIVO exitosamente", idUsuario);

        return MessageResponse.success("Usuario eliminado exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerCliente(String idCliente) {
        log.info("Obteniendo información del cliente: {}", idCliente);

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + idCliente));

        return usuarioMapper.toClienteResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponse obtenerProveedor(String idProveedor) {
        log.info("Obteniendo información del proveedor: {}", idProveedor);

        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + idProveedor));

        return usuarioMapper.toProveedorResponse(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCorreo(String correo) {
        log.debug("Verificando existencia del correo: {}", correo);
        return usuarioRepository.existsByCorreo(correo);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Crea una Specification dinámica basada en los filtros
     */
    private Specification<Usuario> crearSpecification(FiltroUsuarioRequest filtros) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por nombre (búsqueda parcial, case-insensitive)
            if (filtros.getNombre() != null && !filtros.getNombre().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")),
                        "%" + filtros.getNombre().toLowerCase() + "%"
                ));
            }

            // Filtro por correo (búsqueda parcial, case-insensitive)
            if (filtros.getCorreo() != null && !filtros.getCorreo().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("correo")),
                        "%" + filtros.getCorreo().toLowerCase() + "%"
                ));
            }

            // Filtro por rol
            if (filtros.getRol() != null) {
                predicates.add(criteriaBuilder.equal(root.get("rol"), filtros.getRol()));
            }

            // Filtro por estado
            if (filtros.getEstado() != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), filtros.getEstado()));
            }

            // Filtro por rango de fechas de registro
            if (filtros.getFechaRegistroDesde() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("fechaRegistro"),
                        filtros.getFechaRegistroDesde()
                ));
            }

            if (filtros.getFechaRegistroHasta() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("fechaRegistro"),
                        filtros.getFechaRegistroHasta()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}