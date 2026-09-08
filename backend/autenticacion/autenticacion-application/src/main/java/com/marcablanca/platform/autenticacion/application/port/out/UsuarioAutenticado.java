package com.marcablanca.platform.autenticacion.application.port.out;

import java.util.UUID;

/**
 * Lo que se extrae de un JWT ya verificado -- el usuario, la empresa a la que
 * pertenece, y si el token se emitio con contrasena temporal (obliga a cambiarla
 * antes de poder usar cualquier otro endpoint).
 */
public record UsuarioAutenticado(UUID usuarioId, String identificadorEmpresa, boolean debeCambiarContrasena) {
}
