package com.sm_sport.repository;

import com.sm_sport.model.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, String> {

    // Comprobante por pago
    Optional<Comprobante> findByPagoIdPago(String idPago);

    // Comprobante por URL
    Optional<Comprobante> findByUrlArchivo(String urlArchivo);
}
