package com.sm_sport.service.impl;

import com.sm_sport.exception.ResourceNotFoundException;
import com.sm_sport.model.entity.Pago;
import com.sm_sport.model.entity.Reserva;
import com.sm_sport.repository.PagoRepository;
import com.sm_sport.repository.ReservaRepository;
import com.sm_sport.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {


    private final JavaMailSender mailSender;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;

    @Value("${app.name:SM Sport}")
    private String appName;

    @Value("${app.email.from:noreply@smsport.com}")
    private String emailFrom;

    @Value("${app.email.from-name:SM Sport - Santa Marta}")
    private String emailFromName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.frontend.password-reset-path:/reset-password}")
    private String passwordResetPath;

    // Formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    @Override
    @Async
    public void enviarEmailRegistro(String destinatario, String nombre) {
        log.info("Enviando email de bienvenida a: {}", destinatario);

        try {
            String asunto = "¡Bienvenido a " + appName + "!";
            String contenido = construirEmailBienvenida(nombre);

            enviarEmailHtml(destinatario, asunto, contenido);

            log.info("Email de bienvenida enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida a {}: {}", destinatario, e.getMessage());
            // No lanzamos excepción para no afectar el flujo principal
        }
    }

    @Override
    @Async
    public void enviarEmailConfirmacionReserva(String destinatario, String idReserva) {
        log.info("Enviando email de confirmación de reserva {} a: {}", idReserva, destinatario);

        try {
            Reserva reserva = reservaRepository.findById(idReserva)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Reserva no encontrada con ID: " + idReserva));

            String asunto = "Reserva Confirmada - " + reserva.getServicio().getNombre();
            String contenido = construirEmailConfirmacionReserva(reserva);

            enviarEmailHtml(destinatario, asunto, contenido);

            log.info("Email de confirmación de reserva enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email de confirmación de reserva a {}: {}",
                    destinatario, e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarEmailCancelacion(String destinatario, String idReserva) {
        log.info("Enviando email de cancelación de reserva {} a: {}", idReserva, destinatario);

        try {
            Reserva reserva = reservaRepository.findById(idReserva)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Reserva no encontrada con ID: " + idReserva));

            String asunto = "Reserva Cancelada - " + reserva.getServicio().getNombre();
            String contenido = construirEmailCancelacion(reserva);

            enviarEmailHtml(destinatario, asunto, contenido);

            log.info("Email de cancelación enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email de cancelación a {}: {}", destinatario, e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarEmailRecuperacion(String destinatario, String token) {
        log.info("Enviando email de recuperación de contraseña a: {}", destinatario);

        try {
            String asunto = "Recuperación de Contraseña - " + appName;
            String contenido = construirEmailRecuperacion(token);

            enviarEmailHtml(destinatario, asunto, contenido);

            log.info("Email de recuperación enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación a {}: {}", destinatario, e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarComprobantePago(String destinatario, String idPago) {
        log.info("Enviando comprobante de pago {} a: {}", idPago, destinatario);

        try {
            Pago pago = pagoRepository.findById(idPago)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Pago no encontrado con ID: " + idPago));

            String asunto = "Comprobante de Pago - Reserva #" +
                    pago.getReserva().getIdReserva().substring(0, 8).toUpperCase();
            String contenido = construirEmailComprobantePago(pago);

            enviarEmailHtml(destinatario, asunto, contenido);

            log.info("Comprobante de pago enviado exitosamente a: {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar comprobante de pago a {}: {}", destinatario, e.getMessage());
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void enviarEmailHtml(String destinatario, String asunto, String contenidoHtml)
            throws MessagingException, UnsupportedEncodingException {

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setFrom(emailFrom, emailFromName);
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(contenidoHtml, true); // true = es HTML

        mailSender.send(mensaje);
    }

    // ==================== TEMPLATES HTML ====================

    private String construirEmailBienvenida(String nombre) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                                  color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .button { display: inline-block; padding: 12px 30px; background: #667eea; 
                                  color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                        .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Bienvenido a %s!</h1>
                        </div>
                        <div class="content">
                            <h2>Hola %s,</h2>
                            <p>¡Gracias por registrarte en nuestra plataforma de servicios deportivos en Santa Marta!</p>
                            <p>Ahora puedes:</p>
                            <ul>
                                <li>✅ Explorar servicios deportivos</li>
                                <li>✅ Reservar canchas, clases y eventos</li>
                                <li>✅ Gestionar tus reservas</li>
                                <li>✅ Calificar y comentar servicios</li>
                            </ul>
                            <p>Estamos emocionados de tenerte con nosotros.</p>
                            <a href="%s" class="button">Explorar Servicios</a>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s - Todos los derechos reservados</p>
                            <p>Santa Marta, Colombia</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(appName, nombre, frontendUrl, appName);
    }

    private String construirEmailConfirmacionReserva(Reserva reserva) {
        String fecha = reserva.getFechaReserva().format(DATE_FORMATTER);
        String hora = reserva.getHoraReserva().format(TIME_FORMATTER);
        String servicio = reserva.getServicio().getNombre();
        String proveedor = reserva.getProveedor().getNombre();
        String ubicacion = reserva.getServicio().getUbicacion().getCiudad() + ", " +
                reserva.getServicio().getUbicacion().getDepartamento();
        String direccion = reserva.getServicio().getUbicacion().getDireccion();
        String costo = formatearPrecio(reserva.getCostoTotal());
        String idReserva = reserva.getIdReserva().substring(0, 8).toUpperCase();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); 
                                  color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; }
                        .info-box { background: white; padding: 20px; border-left: 4px solid #11998e; 
                                    margin: 20px 0; border-radius: 5px; }
                        .info-row { display: flex; justify-content: space-between; margin: 10px 0; }
                        .label { font-weight: bold; color: #666; }
                        .value { color: #333; }
                        .alert { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; 
                                 border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✅ Reserva Confirmada</h1>
                            <p style="margin: 0;">ID: #%s</p>
                        </div>
                        <div class="content">
                            <h2>Tu reserva ha sido confirmada</h2>
                            <div class="info-box">
                                <h3 style="margin-top: 0;">Detalles del Servicio</h3>
                                <div class="info-row">
                                    <span class="label">Servicio:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Proveedor:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Fecha:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Hora:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Ubicación:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Dirección:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Costo Total:</span>
                                    <span class="value"><strong>%s</strong></span>
                                </div>
                            </div>
                            <div class="alert">
                                <strong>⏰ Recordatorio:</strong> Por favor llega 10 minutos antes de tu hora de reserva.
                            </div>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s - Todos los derechos reservados</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(idReserva, servicio, proveedor, fecha, hora, ubicacion,
                direccion, costo, appName);
    }

    private String construirEmailCancelacion(Reserva reserva) {
        String fecha = reserva.getFechaReserva().format(DATE_FORMATTER);
        String hora = reserva.getHoraReserva().format(TIME_FORMATTER);
        String servicio = reserva.getServicio().getNombre();
        String idReserva = reserva.getIdReserva().substring(0, 8).toUpperCase();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); 
                                  color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .info-box { background: white; padding: 20px; border-left: 4px solid #f5576c; 
                                    margin: 20px 0; border-radius: 5px; }
                        .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>❌ Reserva Cancelada</h1>
                            <p style="margin: 0;">ID: #%s</p>
                        </div>
                        <div class="content">
                            <h2>Tu reserva ha sido cancelada</h2>
                            <div class="info-box">
                                <p><strong>Servicio:</strong> %s</p>
                                <p><strong>Fecha:</strong> %s</p>
                                <p><strong>Hora:</strong> %s</p>
                            </div>
                            <p>Si realizaste un pago, el reembolso será procesado en los próximos 3-5 días hábiles.</p>
                            <p>Esperamos verte pronto. ¡No dudes en hacer una nueva reserva!</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s - Todos los derechos reservados</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(idReserva, servicio, fecha, hora, appName);
    }

    private String construirEmailRecuperacion(String token) {
        String resetUrl = frontendUrl + passwordResetPath + "?token=" + token;

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                                  color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .button { display: inline-block; padding: 12px 30px; background: #667eea; 
                                  color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .alert { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; 
                                 border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Recuperación de Contraseña</h1>
                        </div>
                        <div class="content">
                            <h2>Restablecer tu contraseña</h2>
                            <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta.</p>
                            <p>Haz clic en el siguiente botón para crear una nueva contraseña:</p>
                            <center>
                                <a href="%s" class="button">Restablecer Contraseña</a>
                            </center>
                            <div class="alert">
                                <strong>⚠️ Importante:</strong> Este enlace expirará en 1 hora por seguridad.
                            </div>
                            <p>Si no solicitaste este cambio, puedes ignorar este correo.</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s - Todos los derechos reservados</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(resetUrl, appName);
    }

    private String construirEmailComprobantePago(Pago pago) {
        Reserva reserva = pago.getReserva();
        String idPago = pago.getIdPago().substring(0, 8).toUpperCase();
        String idReserva = reserva.getIdReserva().substring(0, 8).toUpperCase();
        String fecha = pago.getFechaPago().format(DATETIME_FORMATTER);
        String monto = formatearPrecio(pago.getMonto());
        String metodo = formatearMetodoPago(pago.getMetodoPago().name());
        String referencia = pago.getReferenciaPago();
        String servicio = reserva.getServicio().getNombre();
        String fechaReserva = reserva.getFechaReserva().format(DATE_FORMATTER);
        String horaReserva = reserva.getHoraReserva().format(TIME_FORMATTER);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); 
                                  color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; }
                        .info-box { background: white; padding: 20px; margin: 20px 0; border-radius: 5px; 
                                    box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                        .info-row { display: flex; justify-content: space-between; margin: 10px 0; 
                                    padding-bottom: 10px; border-bottom: 1px solid #eee; }
                        .label { font-weight: bold; color: #666; }
                        .value { color: #333; }
                        .total { font-size: 24px; color: #11998e; font-weight: bold; text-align: center; 
                                 margin: 20px 0; padding: 20px; background: #e8f5e9; border-radius: 5px; }
                        .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>💳 Comprobante de Pago</h1>
                            <p style="margin: 0;">Pago Aprobado</p>
                        </div>
                        <div class="content">
                            <h2>Tu pago ha sido procesado exitosamente</h2>
                            <div class="info-box">
                                <h3 style="margin-top: 0;">Información del Pago</h3>
                                <div class="info-row">
                                    <span class="label">ID Pago:</span>
                                    <span class="value">#%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Fecha:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Método:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Referencia:</span>
                                    <span class="value">%s</span>
                                </div>
                            </div>
                            <div class="info-box">
                                <h3 style="margin-top: 0;">Detalles de la Reserva</h3>
                                <div class="info-row">
                                    <span class="label">ID Reserva:</span>
                                    <span class="value">#%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Servicio:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="info-row">
                                    <span class="label">Fecha Reserva:</span>
                                    <span class="value">%s - %s</span>
                                </div>
                            </div>
                            <div class="total">
                                Total Pagado: %s
                            </div>
                            <p style="text-align: center; color: #666;">
                                Guarda este comprobante para tus registros
                            </p>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s - Todos los derechos reservados</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(idPago, fecha, metodo, referencia, idReserva, servicio,
                fechaReserva, horaReserva, monto, appName);
    }

    // ==================== HELPERS ====================

    private String formatearPrecio(BigDecimal precio) {
        return String.format("$%,.0f COP", precio);
    }

    private String formatearMetodoPago(String metodo) {
        return switch (metodo) {
            case "TARJETA_CREDITO" -> "Tarjeta de Crédito";
            case "TARJETA_DEBITO" -> "Tarjeta de Débito";
            case "TRANSFERENCIA" -> "Transferencia Bancaria";
            case "PSE" -> "PSE";
            case "EFECTIVO" -> "Efectivo";
            default -> metodo;
        };
    }
}