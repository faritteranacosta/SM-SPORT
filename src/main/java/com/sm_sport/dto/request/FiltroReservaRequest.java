package com.sm_sport.dto.request;

import com.sm_sport.model.enums.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiltroReservaRequest {

    private String idCliente;
    private String idProveedor;
    private String idServicio;
    private List<EstadoReserva> estados;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Paginación
    private Integer pagina = 0;
    private Integer tamano = 20;
    private String ordenarPor = "fechaReserva";
    private String direccion = "DESC";
}
