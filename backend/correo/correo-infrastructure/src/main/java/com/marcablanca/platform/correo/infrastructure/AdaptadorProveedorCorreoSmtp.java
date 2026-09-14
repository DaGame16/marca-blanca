package com.marcablanca.platform.correo.infrastructure;

import com.marcablanca.platform.correo.application.port.in.ProbarConfiguracionCorreo;
import com.marcablanca.platform.correo.application.port.out.ProveedorDeCorreo;
import com.marcablanca.platform.correo.application.port.out.RepositorioConfiguracionCorreo;
import com.marcablanca.platform.correo.domain.ConfiguracionCorreoNoEncontradaException;
import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;
import com.marcablanca.platform.correo.domain.EnvioDeCorreoFallidoException;
import com.marcablanca.platform.correo.domain.MensajeDeCorreo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.UUID;

/**
 * Contra JavaMailSender (SMTP estandar) -- funciona con cualquier proveedor
 * que hable SMTP (SES, SendGrid, Mailgun, Gmail, Postfix propio, etc.).
 *
 * Tanto la configuracion (remitente, host, puerto, usuario, seguridad) como
 * la CLAVE viven en plataforma.tbl_config_correo (base de control), cifrada
 * con {@link CifradorDeCorreo} -- un admin puede crear, editar y rotar
 * cuantas configuraciones SMTP quiera desde la consola de operacion, sin
 * tocar el backend ni sus variables de entorno.
 */
@Component
public class AdaptadorProveedorCorreoSmtp implements ProveedorDeCorreo, ProbarConfiguracionCorreo {

    private static final Logger log = LoggerFactory.getLogger(AdaptadorProveedorCorreoSmtp.class);

    private final RepositorioConfiguracionCorreo repositorio;

    public AdaptadorProveedorCorreoSmtp(RepositorioConfiguracionCorreo repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void enviar(MensajeDeCorreo mensaje) {
        ConfiguracionSmtp cfg = repositorio.buscarActiva().orElse(null);
        if (cfg == null) {
            log.warn("Sin config SMTP activa en tbl_config_correo. Correo NO enviado. Para={} asunto={}",
                    mensaje.destinatario().valor(), mensaje.asunto());
            return;
        }

        String clave = repositorio.obtenerClaveDescifrada(cfg.uuid()).orElse(null);
        if (cfg.usuario() != null && (clave == null || clave.isBlank())) {
            log.warn("La config SMTP activa (id={}) exige usuario pero no tiene clave configurada. "
                    + "Correo NO enviado. Para={} asunto={}", cfg.uuid(), mensaje.destinatario().valor(),
                    mensaje.asunto());
            return;
        }

        try {
            enviarConexionSmtp(aDatosConexion(cfg, clave), mensaje.destinatario().valor(), mensaje.asunto(),
                    mensaje.cuerpoHtml());
            log.info("Correo enviado a {}", mensaje.destinatario().valor());
        } catch (Exception e) {
            throw new EnvioDeCorreoFallidoException(
                    "No se pudo enviar el correo a " + mensaje.destinatario().valor(), e);
        }
    }

    @Override
    public void ejecutar(UUID id, String destinatario) {
        ConfiguracionSmtp cfg = repositorio.buscarPorId(id)
                .orElseThrow(() -> new ConfiguracionCorreoNoEncontradaException(id));
        String clave = repositorio.obtenerClaveDescifrada(id).orElse(null);

        try {
            enviarConexionSmtp(aDatosConexion(cfg, clave), destinatario, "Correo de prueba -- Marca Blanca",
                    "Si estas leyendo esto, la configuracion SMTP \"" + cfg.remitenteNombre()
                            + "\" funciona correctamente.");
            log.info("Correo de PRUEBA enviado a {} usando config id={}", destinatario, id);
        } catch (Exception e) {
            throw new EnvioDeCorreoFallidoException("No se pudo enviar el correo de prueba a " + destinatario, e);
        }
    }

    @Override
    public void ejecutarAdHoc(DatosConexion datos, String destinatario) {
        try {
            enviarConexionSmtp(datos, destinatario, "Correo de prueba -- Marca Blanca",
                    "Si estas leyendo esto, la configuracion SMTP \"" + datos.remitenteNombre()
                            + "\" funciona correctamente.");
            log.info("Correo de PRUEBA (ad hoc) enviado a {}", destinatario);
        } catch (Exception e) {
            throw new EnvioDeCorreoFallidoException("No se pudo verificar el envio a " + destinatario, e);
        }
    }

    private static DatosConexion aDatosConexion(ConfiguracionSmtp cfg, String clave) {
        return new DatosConexion(cfg.remitenteNombre(), cfg.remitenteCorreo(), cfg.host(), cfg.puerto(),
                cfg.usuario(), cfg.seguridad(), clave);
    }

    private void enviarConexionSmtp(DatosConexion datos, String destinatario, String asunto, String cuerpo)
            throws MessagingException {
        // Los 3 llamadores atrapan Exception (no solo MessagingException) a
        // proposito: sender.send(mime) mas abajo puede lanzar MailException
        // (unchecked) por fallas reales de conexion/autenticacion SMTP, y
        // ambos casos deben terminar igual en EnvioDeCorreoFallidoException.
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(datos.host());
        sender.setPort(datos.puerto());
        if (datos.usuario() != null) {
            sender.setUsername(datos.usuario());
            sender.setPassword(datos.clave());
        }
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(datos.usuario() != null));
        if ("starttls".equalsIgnoreCase(datos.seguridad())) {
            props.put("mail.smtp.starttls.enable", "true");
        } else if ("ssl".equalsIgnoreCase(datos.seguridad())) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        MimeMessage mime = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
        String remitente = datos.remitenteNombre() != null
                ? datos.remitenteNombre() + " <" + datos.remitenteCorreo() + ">" : datos.remitenteCorreo();
        helper.setFrom(remitente);
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(cuerpo, true);
        sender.send(mime);
    }
}
