
package com.marcos.proyecto.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class CorreoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String correoRemitente;

    public CorreoService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ENVIAR COMPROBANTE PDF AL CLIENTE
    public void enviarBoleta(
            String correoDestino,
            String nombreCliente,
            String numeroBoleta,
            byte[] archivoPDF) {

        // VALIDAR CORREO
        if (correoDestino == null || correoDestino.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar el correo del cliente."
            );
        }

        if (archivoPDF == null || archivoPDF.length == 0) {
            throw new IllegalArgumentException(
                    "El archivo PDF está vacío."
            );
        }

        if (correoRemitente == null || correoRemitente.isBlank()) {
            throw new IllegalStateException(
                    "No está configurado el correo remitente de Gmail."
            );
        }

        try {

            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            // REMITENTE Y DESTINATARIO
            helper.setFrom(correoRemitente);
            helper.setTo(correoDestino.trim());

            // ASUNTO
            helper.setSubject(
                    "Comprobante de venta " + numeroBoleta
            );

            // CUERPO DEL MENSAJE
            String nombre = nombreCliente != null
                    && !nombreCliente.isBlank()
                    ? nombreCliente
                    : "Cliente";

            String cuerpo =
                    "<html>"
                    + "<body style='font-family:Arial,sans-serif;'>"
                    + "<h2>TIENDA DE ABARROTES</h2>"
                    + "<p>Estimado/a " + escaparHTML(nombre) + ",</p>"
                    + "<p>Gracias por realizar su compra.</p>"
                    + "<p>Adjuntamos su comprobante de venta "
                    + "<strong>" + escaparHTML(numeroBoleta) + "</strong>."
                    + "</p>"
                    + "<p>Puede descargar el documento PDF adjunto.</p>"
                    + "<br>"
                    + "<p>Atentamente,<br>"
                    + "<strong>TIENDA DE ABARROTES</strong></p>"
                    + "</body>"
                    + "</html>";

            helper.setText(cuerpo, true);

            // ADJUNTAR PDF
            helper.addAttachment(
                    "Comprobante-" + numeroBoleta + ".pdf",
                    new ByteArrayResource(archivoPDF),
                    "application/pdf"
            );

            // ENVIAR CORREO
            mailSender.send(mensaje);

        } catch (MessagingException | MailException e) {

            throw new IllegalStateException(
                    "No se pudo enviar el correo. Verifique la configuración de Gmail.",
                    e
            );
        }
    }

    // PROTEGER TEXTO INSERTADO EN EL HTML DEL CORREO
    private String escaparHTML(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
