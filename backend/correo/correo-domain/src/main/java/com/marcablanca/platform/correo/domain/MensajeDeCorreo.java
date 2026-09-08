package com.marcablanca.platform.correo.domain;

/** Lo que finalmente se manda -- ya resuelto (plantilla renderizada, destinatario validado). */
public record MensajeDeCorreo(DireccionCorreo destinatario, String asunto, String cuerpoHtml) {
}
