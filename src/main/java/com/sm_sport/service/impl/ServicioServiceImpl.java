package com.sm_sport.service.impl;

import com.sm_sport.dto.request.ActualizarServicioRequest;
import com.sm_sport.dto.request.BusquedaServicioRequest;
import com.sm_sport.dto.request.CrearServicioRequest;
import com.sm_sport.dto.request.DisponibilidadRequest;
import com.sm_sport.dto.response.MessageResponse;
import com.sm_sport.dto.response.PageResponse;
import com.sm_sport.dto.response.ServicioDetalleResponse;
import com.sm_sport.dto.response.ServicioResponse;
import com.sm_sport.exception.BusinessException;
import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.mapper.DisponibilidadMapper;
import com.sm_sport.mapper.PageMapper;
import com.sm_sport.mapper.ServicioMapper;
import com.sm_sport.mapper.UbicacionMapper;
import com.sm_sport.model.entity.DisponibilidadServicio;
import com.sm_sport.model.entity.Proveedor;
import com.sm_sport.model.entity.Servicio;
import com.sm_sport.model.entity.UbicacionServicio;
import com.sm_sport.model.enums.EstadoReserva;
import com.sm_sport.model.enums.EstadoServicio;
import com.sm_sport.repository.DisponibilidadServicioRepository;
import com.sm_sport.repository.ProveedorRepository;
import com.sm_sport.repository.ServicioRepository;
import com.sm_sport.repository.UbicacionServicioRepository;
import com.sm_sport.service.ServicioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final ProveedorRepository proveedorRepository;
    private final UbicacionServicioRepository ubicacionRepository;
    private final DisponibilidadServicioRepository disponibilidadRepository;
    private final ServicioMapper servicioMapper;
    private final UbicacionMapper ubicacionMapper;
    private final DisponibilidadMapper disponibilidadMapper;
    private final PageMapper pageMapper;

    @Override
    public ServicioResponse publicarServicio(String idProveedor, CrearServicioRequest request) {
        log.info("Publicando servicio para proveedor: {}", idProveedor);

        // Verificar que el proveedor existe
        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        // Validar saldo (si aplica lógica de negocio)
        if (proveedor.getSaldoCuenta().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Debe recargar saldo para publicar servicios");
        }

        // Crear servicio
        Servicio servicio = servicioMapper.toEntity(request);
        servicio.setProveedor(proveedor);
        servicio.setEstado(EstadoServicio.PUBLICADO);

        // Guardar servicio
        servicio = servicioRepository.save(servicio);

        // Crear ubicación
        if (request.getUbicacion() != null) {
            UbicacionServicio ubicacion = ubicacionMapper.toEntity(request.getUbicacion());
            ubicacion.setServicio(servicio);
            ubicacionRepository.save(ubicacion);
        }

        // Crear disponibilidades
        if (request.getDisponibilidad() != null && !request.getDisponibilidad().isEmpty()) {
            List<DisponibilidadServicio> disponibilidades =
                    disponibilidadMapper.toEntityList(request.getDisponibilidad());

            Servicio finalServicio = servicio;
            disponibilidades.forEach(d -> d.setServicio(finalServicio));
            disponibilidadRepository.saveAll(disponibilidades);
        }

        // Actualizar contador del proveedor
        proveedor.setTotalServiciosPublicados(proveedor.getTotalServiciosPublicados() + 1);
        proveedorRepository.save(proveedor);

        log.info("Servicio publicado exitosamente: {}", servicio.getIdServicio());

        return servicioMapper.toResponse(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioResponse obtenerPorId(String idServicio) {
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        return servicioMapper.toResponse(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioDetalleResponse obtenerDetalle(String idServicio) {
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        return servicioMapper.toDetalleResponse(servicio);
    }

    @Override
    public ServicioResponse actualizarServicio(String idServicio, ActualizarServicioRequest request) {
        log.info("Actualizando servicio: {}", idServicio);

        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // Actualizar campos
        servicioMapper.updateEntityFromRequest(request, servicio);

        // Actualizar ubicación si viene
        if (request.getUbicacion() != null && servicio.getUbicacion() != null) {
            ubicacionMapper.updateEntityFromRequest(request.getUbicacion(), servicio.getUbicacion());
        }

        servicio = servicioRepository.save(servicio);

        log.info("Servicio actualizado exitosamente: {}", idServicio);

        return servicioMapper.toResponse(servicio);
    }

    @Override
    public ServicioResponse cambiarEstado(String idServicio, EstadoServicio nuevoEstado) {
        log.info("Cambiando estado de servicio {} a {}", idServicio, nuevoEstado);

        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        servicio.setEstado(nuevoEstado);
        servicio = servicioRepository.save(servicio);

        return servicioMapper.toResponse(servicio);
    }

    @Override
    public MessageResponse eliminarServicio(String idServicio) {
        log.info("Eliminando servicio: {}", idServicio);

        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // Validar que no tenga reservas activas
        boolean tieneReservasActivas = servicio.getReservas().stream()
                .anyMatch(r -> r.getEstado() == EstadoReserva.CONFIRMADA
                        || r.getEstado() == EstadoReserva.PENDIENTE);

        if (tieneReservasActivas) {
            throw new BusinessException("No se puede eliminar un servicio con reservas activas");
        }

        // Soft delete
        servicio.setEstado(EstadoServicio.ELIMINADO);
        servicioRepository.save(servicio);

        log.info("Servicio eliminado exitosamente: {}", idServicio);

        return MessageResponse.success("Servicio eliminado exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServicioResponse> listarServicios(Integer pagina, Integer tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("fechaPublicacion").descending());

        Page<Servicio> servicios = servicioRepository.findByEstado(EstadoServicio.PUBLICADO, pageable);

        Page<ServicioResponse> serviciosResponse = servicios.map(servicioMapper::toResponse);

        return pageMapper.toPageResponse(serviciosResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponse> listarPorProveedor(String idProveedor) {
        List<Servicio> servicios = servicioRepository.findByProveedorIdUsuario(idProveedor);
        return servicioMapper.toResponseList(servicios);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ServicioResponse> buscarServicios(BusquedaServicioRequest filtros) {
        log.info("Buscando servicios con filtros: {}", filtros);

        Pageable pageable = PageRequest.of(
                filtros.getPagina(),
                filtros.getTamano(),
                Sort.by(Sort.Direction.fromString(filtros.getDireccion()), filtros.getOrdenarPor())
        );

        Page<Servicio> servicios = servicioRepository.buscarConFiltros(
                filtros.getDeporte(),
                filtros.getCiudad(),
                filtros.getPrecioMin(),
                filtros.getPrecioMax(),
                pageable
        );

        Page<ServicioResponse> serviciosResponse = servicios.map(servicioMapper::toResponse);

        return pageMapper.toPageResponse(serviciosResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponse> buscarServiciosCercanos(Double latitud, Double longitud, Integer radioKm) {
        log.info("Buscando servicios cercanos a: {}, {} (radio: {} km)", latitud, longitud, radioKm);

        List<UbicacionServicio> ubicaciones = ubicacionRepository.findServiciosCercanos(
                BigDecimal.valueOf(latitud),
                BigDecimal.valueOf(longitud),
                radioKm
        );

        return ubicaciones.stream()
                .map(UbicacionServicio::getServicio)
                .filter(s -> s.getEstado() == EstadoServicio.PUBLICADO)
                .map(servicioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponse agregarDisponibilidad(String idServicio, List<DisponibilidadRequest> disponibilidades) {
        log.info("Agregando disponibilidad para servicio: {}", idServicio);

        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        List<DisponibilidadServicio> disponibilidadList = disponibilidadMapper.toEntityList(disponibilidades);
        disponibilidadList.forEach(d -> d.setServicio(servicio));

        disponibilidadRepository.saveAll(disponibilidadList);

        log.info("Disponibilidad agregada exitosamente: {} registros", disponibilidadList.size());

        return MessageResponse.success("Disponibilidad agregada exitosamente");
    }
}
