package com.sm_sport.mapper;

import com.sm_sport.dto.request.CrearDenunciaRequest;
import com.sm_sport.dto.response.DenunciaResponse;
import com.sm_sport.model.entity.Denuncia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DenunciaMapper {

    // Denuncia a DenunciaResponse
    @Mapping(source = "usuarioDenunciante.idUsuario", target = "idUsuarioDenunciante")
    @Mapping(source = "usuarioDenunciante.nombre", target = "nombreDenunciante")
    @Mapping(source = "usuarioDenunciado.idUsuario", target = "idUsuarioDenunciado")
    @Mapping(source = "usuarioDenunciado.nombre", target = "nombreDenunciado")
    @Mapping(source = "administrador.idUsuario", target = "idAdministrador")
    @Mapping(source = "administrador.nombre", target = "nombreAdministrador")
    DenunciaResponse toResponse(Denuncia denuncia);

    List<DenunciaResponse> toResponseList(List<Denuncia> denuncias);

    // CrearDenunciaRequest a Denuncia
    @Mapping(target = "idDenuncia", ignore = true)
    @Mapping(target = "usuarioDenunciante", ignore = true)
    @Mapping(target = "usuarioDenunciado", ignore = true)
    @Mapping(target = "fechaDenuncia", ignore = true)
    @Mapping(target = "estado", constant = "PENDIENTE")
    @Mapping(target = "respuestaAdmin", ignore = true)
    @Mapping(target = "accionTomada", ignore = true)
    @Mapping(target = "fechaRespuesta", ignore = true)
    @Mapping(target = "administrador", ignore = true)
    Denuncia toEntity(CrearDenunciaRequest request);
}
