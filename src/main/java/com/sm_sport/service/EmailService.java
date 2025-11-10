package com.sm_sport.service;

public interface EmailService {

    /**
     * Envía email de confirmación de registro
     *
     * @param destinatario Email del destinatario
     * @param nombre       Nombre del usuario
     */
    void enviarEmailRegistro(String destinatario, String nombre);

    /**
     * Envía email de confirmación de reserva
     *
     * @param destinatario Email del destinatario
     * @param idReserva    ID de la reserva
     */
    void enviarEmailConfirmacionReserva(String destinatario, String idReserva);

    /**
     * Envía email de cancelación
     *
     * @param destinatario Email del destinatario
     * @param idReserva    ID de la reserva
     */
    void enviarEmailCancelacion(String destinatario, String idReserva);

    /**
     * Envía email de recuperación de contraseña
     *
     * @param destinatario Email del destinatario
     * @param token        Token de recuperación
     */
    void enviarEmailRecuperacion(String destinatario, String token);

    /**
     * Envía comprobante de pago por email
     *
     * @param destinatario Email del destinatario
     * @param idPago       ID del pago
     */
    void enviarComprobantePago(String destinatario, String idPago);
}
