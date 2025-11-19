package com.sm_sport.service.impl;

import com.sm_sport.dto.request.CrearDenunciaRequest;
import com.sm_sport.dto.request.ResponderDenunciaRequest;
import com.sm_sport.dto.response.DenunciaResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.DenunciaMapper;
import com.sm_sport.model.entity.Administrador;
import com.sm_sport.model.entity.Denuncia;
import com.sm_sport.model.entity.Usuario;
import com.sm_sport.model.enums.EstadoDenuncia;
import com.sm_sport.repository.AdministradorRepository;
import com.sm_sport.repository.DenunciaRepository;
import com.sm_sport.repository.UsuarioRepository;
import com.sm_sport.service.DenunciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DenunciaServiceImpl implements DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;
    private final DenunciaMapper denunciaMapper;

    @Override
    @Transactional
    public DenunciaResponse crearDenuncia(String idDenunciante, CrearDenunciaRequest request) {
        log.info("Creando denuncia del usuario {} contra usuario {}",
                idDenunciante, request.getIdUsuarioDenunciado());

        // Validar que el denunciante existe
        Usuario denunciante = usuarioRepository.findById(idDenunciante)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario denunciante no encontrado con ID: " + idDenunciante));

        // Validar que el denunciado existe
        Usuario denunciado = usuarioRepository.findById(request.getIdUsuarioDenunciado())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario denunciado no encontrado con ID: " + request.getIdUsuarioDenunciado()));

        // Validar que no se denuncie a sí mismo
        if (idDenunciante.equals(request.getIdUsuarioDenunciado())) {
            throw new BusinessException("No puedes denunciarte a ti mismo");
        }

        // Crear la denuncia
        Denuncia denuncia = denunciaMapper.toEntity(request);
        denuncia.setUsuarioDenunciante(denunciante);
        denuncia.setUsuarioDenunciado(denunciado);
        denuncia.setEstado(EstadoDenuncia.PENDIENTE);
        denuncia.setFechaDenuncia(LocalDateTime.now());

        // Guardar
        Denuncia denunciaGuardada = denunciaRepository.save(denuncia);

        log.info("Denuncia creada exitosamente con ID: {}", denunciaGuardada.getIdDenuncia());

        return denunciaMapper.toResponse(denunciaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public DenunciaResponse obtenerPorId(String idDenuncia) {
        log.info("Obteniendo denuncia con ID: {}", idDenuncia);

        Denuncia denuncia = denunciaRepository.findById(idDenuncia)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Denuncia no encontrada con ID: " + idDenuncia));

        return denunciaMapper.toResponse(denuncia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaResponse> listarPorDenunciante(String idUsuario) {
        log.info("Listando denuncias realizadas por usuario: {}", idUsuario);

        // Validar que el usuario existe
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario);
        }

        List<Denuncia> denuncias = denunciaRepository.findByUsuarioDenuncianteIdUsuario(idUsuario);

        log.info("Se encontraron {} denuncias del usuario {}", denuncias.size(), idUsuario);

        return denunciaMapper.toResponseList(denuncias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaResponse> listarContraUsuario(String idUsuario) {
        log.info("Listando denuncias contra usuario: {}", idUsuario);

        // Validar que el usuario existe
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario);
        }

        List<Denuncia> denuncias = denunciaRepository.findByUsuarioDenunciadoIdUsuario(idUsuario);

        log.info("Se encontraron {} denuncias contra el usuario {}", denuncias.size(), idUsuario);

        return denunciaMapper.toResponseList(denuncias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DenunciaResponse> listarPendientes() {
        log.info("Listando denuncias pendientes");

        List<Denuncia> denuncias = denunciaRepository.findDenunciasPendientes();

        log.info("Se encontraron {} denuncias pendientes", denuncias.size());

        return denunciaMapper.toResponseList(denuncias);
    }

    @Override
    @Transactional
    public DenunciaResponse responderDenuncia(String idDenuncia, String idAdmin,
                                              ResponderDenunciaRequest request) {
        log.info("Administrador {} respondiendo denuncia {}", idAdmin, idDenuncia);

        // Validar que la denuncia existe
        Denuncia denuncia = denunciaRepository.findById(idDenuncia)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Denuncia no encontrada con ID: " + idDenuncia));

        // Validar que el administrador existe
        Administrador administrador = administradorRepository.findById(idAdmin)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Administrador no encontrado con ID: " + idAdmin));

        // Validar que la denuncia esté en estado PENDIENTE o EN_REVICION
        if (denuncia.getEstado() == EstadoDenuncia.ATENDIDA ||
                denuncia.getEstado() == EstadoDenuncia.IMPROCEDENTE) {
            throw new BusinessException("Esta denuncia ya fue atendida previamente");
        }

        // Validar que se proporcione una acción
        if (request.getAccionTomada() == null) {
            throw new BusinessException("Debe especificar una acción a tomar");
        }

        // Actualizar la denuncia
        denuncia.setRespuestaAdmin(request.getRespuesta());
        denuncia.setAccionTomada(request.getAccionTomada());
        denuncia.setEstado(EstadoDenuncia.ATENDIDA);
        denuncia.setFechaRespuesta(LocalDateTime.now());
        denuncia.setAdministrador(administrador);

        // Guardar
        Denuncia denunciaActualizada = denunciaRepository.save(denuncia);

        log.info("Denuncia {} atendida exitosamente por administrador {}",
                idDenuncia, idAdmin);

        return denunciaMapper.toResponse(denunciaActualizada);
    }

    @Override
    @Transactional
    public DenunciaResponse declararImprocedente(String idDenuncia, String idAdmin) {
        log.info("Administrador {} declarando denuncia {} como improcedente",
                idAdmin, idDenuncia);

        // Validar que la denuncia existe
        Denuncia denuncia = denunciaRepository.findById(idDenuncia)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Denuncia no encontrada con ID: " + idDenuncia));

        // Validar que el administrador existe
        Administrador administrador = administradorRepository.findById(idAdmin)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Administrador no encontrado con ID: " + idAdmin));

        // Validar que la denuncia no esté ya atendida
        if (denuncia.getEstado() == EstadoDenuncia.ATENDIDA ||
                denuncia.getEstado() == EstadoDenuncia.IMPROCEDENTE) {
            throw new BusinessException("Esta denuncia ya fue procesada previamente");
        }

        // Marcar como improcedente
        denuncia.setEstado(EstadoDenuncia.IMPROCEDENTE);
        denuncia.setRespuestaAdmin("Denuncia declarada como improcedente tras revisión");
        denuncia.setFechaRespuesta(LocalDateTime.now());
        denuncia.setAdministrador(administrador);
        denuncia.setAccionTomada(null); // Sin acción porque es improcedente

        // Guardar
        Denuncia denunciaActualizada = denunciaRepository.save(denuncia);

        log.info("Denuncia {} declarada improcedente por administrador {}",
                idDenuncia, idAdmin);

        return denunciaMapper.toResponse(denunciaActualizada);
    }
}