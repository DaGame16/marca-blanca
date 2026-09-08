package com.marcablanca.platform.correo.infrastructure;

import com.marcablanca.platform.correo.application.port.out.ProveedorDeCorreo;
import com.marcablanca.platform.correo.domain.EnvioDeCorreoFallidoException;
import com.marcablanca.platform.correo.domain.MensajeDeCorreo;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Contra JavaMailSender (SMTP estandar) -- funciona con cualquier proveedor
 * que hable SMTP (SES, SendGrid, Mailgun, Gmail, Postfix propio, etc.).
 *
 * La configuracion (remitente, host, puerto, usuario, tipo de seguridad) se
 * lee de plataforma.tbl_config_correo (base de control), no de propiedades
 * estaticas -- asi coincide con lo que ya administra Leidi ahi. La clave en
 * si sigue viniendo de app.aprovisionamiento.smtp-password (se mantuvo el
 * mismo nombre de propiedad al mudar esta pieza, para no romper ninguna
 * variable de entorno ya configurada en ningun ambiente).
 *
 * Si no hay ninguna fila con es_activa=true, o falta la clave: se loguea una
 * advertencia y se sale sin lanzar excepcion -- mismo comportamiento que
 * tenia esto cuando vivia adentro de aprovisionamiento, para no tumbar el
 * pipeline de alta de una empresa solo porque el correo todavia no esta
 * configurado en ese ambiente.
 */
@Component
public class AdaptadorProveedorCorreoSmtp implements ProveedorDeCorreo {

    private static final Logger log = LoggerFactory.getLogger(AdaptadorProveedorCorreoSmtp.class);

    private final JdbcTemplate control;
    private final String smtpClave;

    public AdaptadorProveedorCorreoSmtp(@Qualifier("controlDataSource") DataSource controlDataSource,
                                         @Value("${app.aprovisionamiento.smtp-password:}") String smtpClave) {
        this.control = new JdbcTemplate(controlDataSource);
        this.smtpClave = smtpClave;
    }

    @Override
    public void enviar(MensajeDeCorreo mensaje) {
        String[] cfg = control.query("""
                select remitente_nombre, remitente_correo, host, puerto, usuario, seguridad
                from plataforma.tbl_config_correo where es_activa limit 1""",
                rs -> rs.next() ? new String[] {
                        rs.getString(1), rs.getString(2), rs.getString(3),
                        String.valueOf(rs.getInt(4)), rs.getString(5), rs.getString(6)
                } : null);

        if (cfg == null || smtpClave == null || smtpClave.isBlank()) {
            log.warn("Sin config SMTP activa en tbl_config_correo (o sin app.aprovisionamiento.smtp-password). "
                    + "Correo NO enviado. Para={} asunto={}", mensaje.destinatario().valor(), mensaje.asunto());
            return;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(cfg[2]);
        sender.setPort(Integer.parseInt(cfg[3]));
        if (cfg[4] != null) {
            sender.setUsername(cfg[4]);
            sender.setPassword(smtpClave);
        }
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(cfg[4] != null));
        if ("starttls".equalsIgnoreCase(cfg[5])) {
            props.put("mail.smtp.starttls.enable", "true");
        } else if ("ssl".equalsIgnoreCase(cfg[5])) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        try {
            MimeMessage mime = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
            String remitente = cfg[0] != null ? cfg[0] + " <" + cfg[1] + ">" : cfg[1];
            helper.setFrom(remitente);
            helper.setTo(mensaje.destinatario().valor());
            helper.setSubject(mensaje.asunto());
            helper.setText(mensaje.cuerpoHtml(), true);
            sender.send(mime);
            log.info("Correo enviado a {}", mensaje.destinatario().valor());
        } catch (Exception e) {
            throw new EnvioDeCorreoFallidoException(
                    "No se pudo enviar el correo a " + mensaje.destinatario().valor(), e);
        }
    }
}
