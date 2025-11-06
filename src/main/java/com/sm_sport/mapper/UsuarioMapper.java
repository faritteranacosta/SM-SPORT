package com.sm_sport.mapper;

import com.sm_sport.dto.request.ActualizarPerfilRequest;
import com.sm_sport.dto.request.RegistroUsuarioRequest;
import com.sm_sport.dto.response.ClienteResponse;
import com.sm_sport.dto.response.ProveedorResponse;
import com.sm_sport.dto.response.UsuarioResponse;
import com.sm_sport.model.entity.Cliente;
import com.sm_sport.model.entity.Proveedor;
import com.sm_sport.model.entity.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsuarioMapper {

    // Usuario a UsuarioResponse
    UsuarioResponse toResponse(Usuario usuario);

    List<UsuarioResponse> toResponseList(List<Usuario> usuarios);

    // Cliente a ClienteResponse
    @Mapping(target = "totalReservas", expression = "java(cliente.getHistorialReservas() != null ? cliente.getHistorialReservas().size() : 0)")
    @Mapping(target = "resenasPublicadas", expression = "java(cliente.getResenas() != null ? cliente.getResenas().size() : 0)")
    ClienteResponse toClienteResponse(Cliente cliente);

    // Proveedor a ProveedorResponse
    ProveedorResponse toProveedorResponse(Proveedor proveedor);

    // RegistroUsuarioRequest a Cliente
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "estado", constant = "ACTIVO")
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "historialReservas", ignore = true)
    @Mapping(target = "resenas", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    @Mapping(target = "solicitudesReembolso", ignore = true)
    Cliente toCliente(RegistroUsuarioRequest request);

    // RegistroUsuarioRequest a Proveedor
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "estado", constant = "ACTIVO")
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "saldoCuenta", constant = "0")
    @Mapping(target = "calificacionPromedio", constant = "0")
    @Mapping(target = "totalServiciosPublicados", constant = "0")
    @Mapping(target = "totalReservasCompletadas", constant = "0")
    @Mapping(target = "verificado", constant = "false")
    @Mapping(target = "serviciosPublicados", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    @Mapping(target = "reportes", ignore = true)
    Proveedor toProveedor(RegistroUsuarioRequest request);

    // Actualizar perfil
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "correo", ignore = true)
    @Mapping(target = "contrasena", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    void updateEntityFromRequest(ActualizarPerfilRequest request, @MappingTarget Usuario usuario);
}
