package com.marcablanca.platform.correo.application.port.out;

import com.marcablanca.platform.correo.domain.MensajeDeCorreo;

/** Unico puerto que sabe que existe un mecanismo real de envio (hoy: SMTP). */
public interface ProveedorDeCorreo {
    void enviar(MensajeDeCorreo mensaje);
}
