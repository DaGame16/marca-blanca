package com.marcablanca.platform.autenticacion.application.port.out;

import java.util.UUID;

/**
 * Lo minimo que autenticacion necesita saber de un usuario -- nunca el tipo
 * Usuario de usuarios-domain. Distinto de UsuarioAutenticado: ese es lo que
 * sale de verificar un JWT (usuario + empresa); este es lo que entra para
 * generar uno.
 */
public record DatosDeUsuario(UUID id, String correo) {
}
