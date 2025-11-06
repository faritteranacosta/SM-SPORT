package com.sm_sport.mapper;

import com.sm_sport.dto.response.NotificacionResponse;
import com.sm_sport.model.entity.Notificacion;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificacionMapper {

    // Notificacion a NotificacionResponse
    NotificacionResponse toResponse(Notificacion notificacion);

    List<NotificacionResponse> toResponseList(List<Notificacion> notificaciones);
}
