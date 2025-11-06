package com.sm_sport.mapper;

import com.sm_sport.dto.request.ActualizarServicioRequest;
import com.sm_sport.dto.request.CrearServicioRequest;
import com.sm_sport.dto.response.ServicioDetalleResponse;
import com.sm_sport.dto.response.ServicioResponse;
import com.sm_sport.model.entity.Resena;
import com.sm_sport.model.entity.Servicio;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {UbicacionMapper.class, DisponibilidadMapper.class, ResenaMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServicioMapper {

    // Servicio a ServicioResponse
    @Mapping(source = "proveedor.idUsuario", target = "idProveedor")
    @Mapping(source = "proveedor.nombre", target = "nombreProveedor")
    @Mapping(source = "proveedor.verificado", target = "proveedorVerificado")
    @Mapping(source = "ubicacion.ciudad", target = "ciudad")
    @Mapping(source = "ubicacion.departamento", target = "departamento")
    @Mapping(source = "ubicacion.direccion", target = "direccion")
    ServicioResponse toResponse(Servicio servicio);

    List<ServicioResponse> toResponseList(List<Servicio> servicios);

    // Servicio a ServicioDetalleResponse
    @Mapping(source = "proveedor", target = "proveedor")
    @Mapping(source = "ubicacion", target = "ubicacion")
    @Mapping(source = "disponibilidad", target = "disponibilidades")
    @Mapping(source = "resenas", target = "resenas", qualifiedByName = "limitResenas")
    ServicioDetalleResponse toDetalleResponse(Servicio servicio);

    // CrearServicioRequest a Servicio
    @Mapping(target = "idServicio", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "estado", constant = "PUBLICADO")
    @Mapping(target = "fechaPublicacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "calificacionPromedio", constant = "0")
    @Mapping(target = "totalResenas", constant = "0")
    @Mapping(target = "reservas", ignore = true)
    @Mapping(target = "resenas", ignore = true)
    Servicio toEntity(CrearServicioRequest request);

    // Actualizar servicio
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "idServicio", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaPublicacion", ignore = true)
    @Mapping(target = "calificacionPromedio", ignore = true)
    @Mapping(target = "totalResenas", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    @Mapping(target = "resenas", ignore = true)
    void updateEntityFromRequest(ActualizarServicioRequest request, @MappingTarget Servicio servicio);

    // Método personalizado para limitar reseñas en detalle
    @Named("limitResenas")
    default List<Resena> limitResenas(
            List<Resena> resenas) {
        if (resenas == null) return null;
        return resenas.stream().limit(5).toList();
    }
}
