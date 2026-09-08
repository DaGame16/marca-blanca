package com.marcablanca.platform.correo.infrastructure;

import com.marcablanca.platform.correo.application.port.out.ProveedorDeCorreo;
import com.marcablanca.platform.correo.domain.EnvioDeCorreoFallidoException;
import com.marcablanca.platform.correo.domain.MensajeDeCorreo;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Contra JavaMailSender (SMTP estandar) -- funciona hoy con cualquier
 * proveedor que hable SMTP (SES, SendGrid, Mailgun, Gmail, Postfix propio,
 * etc.), sin acoplarse a ninguno en particular. El "cual" se resuelve
 * apuntando MAIL_HOST/MAIL_USERNAME/MAIL_PASSWORD a ese proveedor -- sin
 * tocar este archivo.
 */
@Component
public class AdaptadorProveedorCorreoSmtp implements ProveedorDeCorreo {

    private final JavaMailSender javaMailSender;
    private final String remitente;
    private final boolean habilitado;

    public AdaptadorProveedorCorreoSmtp(JavaMailSender javaMailSender,
                                         @Value("${app.correo.remitente:no-responder@marcablanca.local}") String remitente,
                                         @Value("${app.correo.habilitado:false}") boolean habilitado) {
        this.javaMailSender = javaMailSender;
        this.remitente = remitente;
        this.habilitado = habilitado;
    }

    @Override
    public void enviar(MensajeDeCorreo mensaje) {
        if (!habilitado) {
            throw new EnvioDeCorreoFallidoException(
                    "Envio de correo deshabilitado (app.correo.habilitado=false) -- configurar "
                            + "MAIL_HOST/MAIL_USERNAME/MAIL_PASSWORD/CORREO_REMITENTE y CORREO_HABILITADO=true.",
                    null);
        }
        try {
            MimeMessage mime = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(mensaje.destinatario().valor());
            helper.setSubject(mensaje.asunto());
            helper.setText(mensaje.cuerpoHtml(), true);
            javaMailSender.send(mime);
        } catch (Exception e) {
            throw new EnvioDeCorreoFallidoException(
                    "No se pudo enviar el correo a " + mensaje.destinatario().valor(), e);
        }
    }
}
