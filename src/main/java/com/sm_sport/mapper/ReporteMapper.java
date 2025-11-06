package com.sm_sport.mapper;

import com.sm_sport.dto.response.KPIResponse;
import com.sm_sport.dto.response.MetricaResponse;
import com.sm_sport.dto.response.ReporteDesempenoResponse;
import com.sm_sport.model.entity.KPI;
import com.sm_sport.model.entity.MetricaSistema;
import com.sm_sport.model.entity.ReporteDesempeno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReporteMapper {

    // ReporteDesempeno a ReporteDesempenoResponse
    @Mapping(source = "proveedor.idUsuario", target = "idProveedor")
    @Mapping(source = "proveedor.nombre", target = "nombreProveedor")
    ReporteDesempenoResponse toReporteResponse(ReporteDesempeno reporte);

    List<ReporteDesempenoResponse> toReporteResponseList(List<ReporteDesempeno> reportes);

    // MetricaSistema a MetricaResponse
    MetricaResponse toMetricaResponse(MetricaSistema metrica);

    List<MetricaResponse> toMetricaResponseList(List<MetricaSistema> metricas);

    // KPI a KPIResponse
    KPIResponse toKPIResponse(KPI kpi);

    List<KPIResponse> toKPIResponseList(List<KPI> kpis);
}
