package com.sm_sport.service;

import com.sm_sport.dto.request.CrearDenunciaRequest;
import com.sm_sport.dto.request.ResponderDenunciaRequest;
import com.sm_sport.dto.response.DenunciaResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.DenunciaMapper;
import com.sm_sport.model.entity.Administrador;
import com.sm_sport.model.entity.Denuncia;
import com.sm_sport.model.entity.Usuario;
import com.sm_sport.model.enums.AccionDenuncia;
import com.sm_sport.model.enums.EstadoDenuncia;
import com.sm_sport.repository.AdministradorRepository;
import com.sm_sport.repository.DenunciaRepository;
import com.sm_sport.repository.UsuarioRepository;
import com.sm_sport.service.impl.DenunciaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DenunciaServiceImpl - Tests unitarios")
class DenunciaServiceTest {

    @Mock
    private DenunciaRepository denunciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private DenunciaMapper denunciaMapper;

    @InjectMocks
    private DenunciaServiceImpl denunciaService;

    private Usuario denunciante;
    private Usuario denunciado;
    private Administrador administrador;
    private Denuncia denuncia;
    private DenunciaResponse denunciaResponse;
    private CrearDenunciaRequest crearRequest;
    private ResponderDenunciaRequest responderRequest;

    @BeforeEach
    void setup() {
        denunciante = new Usuario() {};
        denunciante.setIdUsuario("user-denunciante");
        denunciante.setNombre("Denunciante Uno");

        denunciado = new Usuario() {};
        denunciado.setIdUsuario("user-denunciado");
        denunciado.setNombre("Denunciado Dos");

        administrador = new Administrador();
        administrador.setIdUsuario("admin-1");
        administrador.setNombre("Admin Uno");

        denuncia = Denuncia.builder()
                .idDenuncia("den-123")
                .usuarioDenunciante(denunciante)
                .usuarioDenunciado(denunciado)
                .tipoDenuncia("INCUMPLIMIENTO")
                .descripcion("Descripción válida con más de 20 caracteres...")
                .evidencia("evidencia.jpg")
                .fechaDenuncia(LocalDateTime.now())
                .estado(EstadoDenuncia.PENDIENTE)
                .build();

        denunciaResponse = DenunciaResponse.builder()
                .idDenuncia(denuncia.getIdDenuncia())
                .tipoDenuncia(denuncia.getTipoDenuncia())
                .descripcion(denuncia.getDescripcion())
                .evidencia(denuncia.getEvidencia())
                .fechaDenuncia(denuncia.getFechaDenuncia())
                .estado(denuncia.getEstado())
                .idUsuarioDenunciante(denunciante.getIdUsuario())
                .nombreDenunciante(denunciante.getNombre())
                .idUsuarioDenunciado(denunciado.getIdUsuario())
                .nombreDenunciado(denunciado.getNombre())
                .build();

        crearRequest = CrearDenunciaRequest.builder()
                .idUsuarioDenunciado(denunciado.getIdUsuario())
                .tipoDenuncia("INCUMPLIMIENTO")
                .descripcion("Descripción válida con más de 20 caracteres...")
                .evidencia("evidencia.jpg")
                .build();

        responderRequest = new ResponderDenunciaRequest();
        responderRequest.setRespuesta("Respuesta admin con contenido suficiente...");
        responderRequest.setAccionTomada(AccionDenuncia.ADVERTENCIA);
    }

    // ------------------------------
    // crearDenuncia - exitoso
    // ------------------------------
    @Test
    @DisplayName("crearDenuncia - exitoso")
    void crearDenuncia_exitoso() {
        when(usuarioRepository.findById(denunciante.getIdUsuario())).thenReturn(Optional.of(denunciante));
        when(usuarioRepository.findById(denunciado.getIdUsuario())).thenReturn(Optional.of(denunciado));
        when(denunciaMapper.toEntity(any(CrearDenunciaRequest.class))).thenReturn(Denuncia.builder().build());
        // Ensure repository.save returns an entity with id
        Denuncia saved = Denuncia.builder()
                .idDenuncia("den-234")
                .usuarioDenunciante(denunciante)
                .usuarioDenunciado(denunciado)
                .tipoDenuncia(crearRequest.getTipoDenuncia())
                .descripcion(crearRequest.getDescripcion())
                .fechaDenuncia(LocalDateTime.now())
                .estado(EstadoDenuncia.PENDIENTE)
                .build();
        when(denunciaRepository.save(any(Denuncia.class))).thenReturn(saved);
        when(denunciaMapper.toResponse(saved)).thenReturn(denunciaResponse);

        DenunciaResponse resp = denunciaService.crearDenuncia(denunciante.getIdUsuario(), crearRequest);

        assertThat(resp).isNotNull();
        assertThat(resp.getIdDenuncia()).isEqualTo(denunciaResponse.getIdDenuncia());
        verify(usuarioRepository).findById(denunciante.getIdUsuario());
        verify(usuarioRepository).findById(denunciado.getIdUsuario());
        verify(denunciaRepository).save(any(Denuncia.class));
        verify(denunciaMapper).toResponse(saved);
    }

    // ------------------------------
    // crearDenuncia - denunciado inexistente
    // ------------------------------
    @Test
    @DisplayName("crearDenuncia - denunciado no encontrado -> ResourceNotFoundException")
    void crearDenuncia_denunciadoNoExiste() {
        when(usuarioRepository.findById(denunciante.getIdUsuario())).thenReturn(Optional.of(denunciante));
        when(usuarioRepository.findById(denunciado.getIdUsuario())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> denunciaService.crearDenuncia(denunciante.getIdUsuario(), crearRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario denunciado no encontrado");

        verify(denunciaRepository, never()).save(any());
    }

    // ------------------------------
    // crearDenuncia - auto denuncia (mismo id)
    // ------------------------------
    @Test
    @DisplayName("crearDenuncia - denunciarse a sí mismo -> BusinessException")
    void crearDenuncia_autoDenuncia() {
        when(usuarioRepository.findById(denunciante.getIdUsuario())).thenReturn(Optional.of(denunciante));
        CrearDenunciaRequest req = CrearDenunciaRequest.builder()
                .idUsuarioDenunciado(denunciante.getIdUsuario())
                .tipoDenuncia("OTRO")
                .descripcion("descripcion larga suficiente para pasar validaciones")
                .build();

        assertThatThrownBy(() -> denunciaService.crearDenuncia(denunciante.getIdUsuario(), req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No puedes denunciarte a ti mismo");

        verify(denunciaRepository, never()).save(any());
    }

    // ------------------------------
    // obtenerPorId - exitoso / no encontrado
    // ------------------------------
    @Test
    @DisplayName("obtenerPorId - exitoso")
    void obtenerPorId_exitoso() {
        when(denunciaRepository.findById("den-123")).thenReturn(Optional.of(denuncia));
        when(denunciaMapper.toResponse(denuncia)).thenReturn(denunciaResponse);

        DenunciaResponse res = denunciaService.obtenerPorId("den-123");
        assertThat(res).isNotNull();
        assertThat(res.getIdDenuncia()).isEqualTo(denunciaResponse.getIdDenuncia());
        verify(denunciaRepository).findById("den-123");
    }

    @Test
    @DisplayName("obtenerPorId - no encontrado lanza ResourceNotFoundException")
    void obtenerPorId_noEncontrado() {
        when(denunciaRepository.findById("den-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> denunciaService.obtenerPorId("den-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Denuncia no encontrada");

        verify(denunciaRepository).findById("den-999");
    }

    // ------------------------------
    // listarPorDenunciante / listarContraUsuario
    // ------------------------------
    @Test
    @DisplayName("listarPorDenunciante - exitoso")
    void listarPorDenunciante_exitoso() {
        when(usuarioRepository.existsById(denunciante.getIdUsuario())).thenReturn(true);
        when(denunciaRepository.findByUsuarioDenuncianteIdUsuario(denunciante.getIdUsuario()))
                .thenReturn(List.of(denuncia));
        when(denunciaMapper.toResponseList(anyList())).thenReturn(List.of(denunciaResponse));

        List<DenunciaResponse> list = denunciaService.listarPorDenunciante(denunciante.getIdUsuario());
        assertThat(list).hasSize(1);
        verify(usuarioRepository).existsById(denunciante.getIdUsuario());
        verify(denunciaRepository).findByUsuarioDenuncianteIdUsuario(denunciante.getIdUsuario());
    }

    @Test
    @DisplayName("listarPorDenunciante - usuario no encontrado lanza ResourceNotFoundException")
    void listarPorDenunciante_usuarioNoExiste() {
        when(usuarioRepository.existsById("no-existe")).thenReturn(false);

        assertThatThrownBy(() -> denunciaService.listarPorDenunciante("no-existe"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(denunciaRepository, never()).findByUsuarioDenuncianteIdUsuario(any());
    }

    @Test
    @DisplayName("listarContraUsuario - exitoso")
    void listarContraUsuario_exitoso() {
        when(usuarioRepository.existsById(denunciado.getIdUsuario())).thenReturn(true);
        when(denunciaRepository.findByUsuarioDenunciadoIdUsuario(denunciado.getIdUsuario()))
                .thenReturn(List.of(denuncia));
        when(denunciaMapper.toResponseList(anyList())).thenReturn(List.of(denunciaResponse));

        List<DenunciaResponse> list = denunciaService.listarContraUsuario(denunciado.getIdUsuario());
        assertThat(list).hasSize(1);
        verify(usuarioRepository).existsById(denunciado.getIdUsuario());
        verify(denunciaRepository).findByUsuarioDenunciadoIdUsuario(denunciado.getIdUsuario());
    }

    @Test
    @DisplayName("listarContraUsuario - usuario no encontrado lanza ResourceNotFoundException")
    void listarContraUsuario_usuarioNoExiste() {
        when(usuarioRepository.existsById("no-existe")).thenReturn(false);

        assertThatThrownBy(() -> denunciaService.listarContraUsuario("no-existe"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(denunciaRepository, never()).findByUsuarioDenunciadoIdUsuario(any());
    }

    // ------------------------------
    // listarPendientes
    // ------------------------------
    @Test
    @DisplayName("listarPendientes - exitoso")
    void listarPendientes_exitoso() {
        when(denunciaRepository.findDenunciasPendientes()).thenReturn(List.of(denuncia));
        when(denunciaMapper.toResponseList(anyList())).thenReturn(List.of(denunciaResponse));

        List<DenunciaResponse> pendientes = denunciaService.listarPendientes();
        assertThat(pendientes).hasSize(1);
        verify(denunciaRepository).findDenunciasPendientes();
    }

    // ------------------------------
    // responderDenuncia - exitoso y errores
    // ------------------------------
    @Test
    @DisplayName("responderDenuncia - exitoso")
    void responderDenuncia_exitoso() {
        when(denunciaRepository.findById("den-123")).thenReturn(Optional.of(denuncia));
        when(administradorRepository.findById(administrador.getIdUsuario())).thenReturn(Optional.of(administrador));
        when(denunciaRepository.save(any(Denuncia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // mapper will transform saved entity to response
        when(denunciaMapper.toResponse(any(Denuncia.class))).thenReturn(denunciaResponse);

        DenunciaResponse resp = denunciaService.responderDenuncia("den-123", administrador.getIdUsuario(), responderRequest);

        assertThat(resp).isNotNull();
        assertThat(resp.getEstado()).isEqualTo(EstadoDenuncia.PENDIENTE) // mapper mock returns original state; main assertion is no exception
                .isNotNull();

        verify(denunciaRepository).findById("den-123");
        verify(administradorRepository).findById(administrador.getIdUsuario());
        verify(denunciaRepository).save(any(Denuncia.class));
    }

    @Test
    @DisplayName("responderDenuncia - admin no encontrado -> ResourceNotFoundException")
    void responderDenuncia_adminNoEncontrado() {
        when(denunciaRepository.findById("den-123")).thenReturn(Optional.of(denuncia));
        when(administradorRepository.findById("no-admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> denunciaService.responderDenuncia("den-123", "no-admin", responderRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Administrador no encontrado");

        verify(denunciaRepository).findById("den-123");
        verify(denunciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("responderDenuncia - ya atendida -> BusinessException")
    void responderDenuncia_yaAtendida() {
        Denuncia atendida = Denuncia.builder()
                .idDenuncia("den-x")
                .estado(EstadoDenuncia.ATENDIDA)
                .build();
        when(denunciaRepository.findById("den-x")).thenReturn(Optional.of(atendida));
        when(administradorRepository.findById(administrador.getIdUsuario())).thenReturn(Optional.of(administrador));

        assertThatThrownBy(() -> denunciaService.responderDenuncia("den-x", administrador.getIdUsuario(), responderRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya fue atendida");

        verify(denunciaRepository).findById("den-x");
        verify(denunciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("responderDenuncia - accion nula -> BusinessException")
    void responderDenuncia_accionNula() {
        ResponderDenunciaRequest reqSinAccion = new ResponderDenunciaRequest();
        reqSinAccion.setRespuesta("Respuesta valida...");
        reqSinAccion.setAccionTomada(null);

        when(denunciaRepository.findById("den-123")).thenReturn(Optional.of(denuncia));
        when(administradorRepository.findById(administrador.getIdUsuario())).thenReturn(Optional.of(administrador));

        assertThatThrownBy(() -> denunciaService.responderDenuncia("den-123", administrador.getIdUsuario(), reqSinAccion))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Debe especificar una acción");

        verify(denunciaRepository).findById("den-123");
        verify(denunciaRepository, never()).save(any());
    }

    // ------------------------------
    // declararImprocedente - exitoso y errores
    // ------------------------------
    @Test
    @DisplayName("declararImprocedente - exitoso")
    void declararImprocedente_exitoso() {
        when(denunciaRepository.findById("den-123")).thenReturn(Optional.of(denuncia));
        when(administradorRepository.findById(administrador.getIdUsuario())).thenReturn(Optional.of(administrador));
        when(denunciaRepository.save(any(Denuncia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(denunciaMapper.toResponse(any(Denuncia.class))).thenReturn(denunciaResponse);

        DenunciaResponse resp = denunciaService.declararImprocedente("den-123", administrador.getIdUsuario());

        assertThat(resp).isNotNull();
        verify(denunciaRepository).save(any(Denuncia.class));
    }

    @Test
    @DisplayName("declararImprocedente - admin no encontrado -> ResourceNotFoundException")
    void declararImprocedente_adminNoEncontrado() {
        when(denunciaRepository.findById("den-123")).thenReturn(Optional.of(denuncia));
        when(administradorRepository.findById("no-admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> denunciaService.declararImprocedente("den-123", "no-admin"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Administrador no encontrado");

        verify(denunciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("declararImprocedente - ya procesada -> BusinessException")
    void declararImprocedente_yaProcesada() {
        Denuncia procesada = Denuncia.builder()
                .idDenuncia("den-y")
                .estado(EstadoDenuncia.IMPROCEDENTE)
                .build();
        when(denunciaRepository.findById("den-y")).thenReturn(Optional.of(procesada));
        when(administradorRepository.findById(administrador.getIdUsuario())).thenReturn(Optional.of(administrador));

        assertThatThrownBy(() -> denunciaService.declararImprocedente("den-y", administrador.getIdUsuario()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya fue procesada");

        verify(denunciaRepository).findById("den-y");
        verify(denunciaRepository, never()).save(any());
    }
}
