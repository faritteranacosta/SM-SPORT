package com.sm_sport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClienteResponse extends UsuarioResponse {

    private String preferenciaDeportes;
    private String nivelExperiencia;
    private LocalDate fechaNacimiento;
    private Integer totalReservas;
    private Integer resenasPublicadas;
}
