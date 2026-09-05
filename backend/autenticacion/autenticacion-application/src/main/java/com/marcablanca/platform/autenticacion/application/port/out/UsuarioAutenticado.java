package com.marcablanca.platform.autenticacion.application.port.out;

import java.util.UUID;

/**
 * Lo que se extrae de un JWT ya verificado -- el usuario Y la empresa a la
 * que pertenece. Antes solo se extraia el usuarioId; sin la empresa, un
 * endpoint que un usuario logueado usa para su PROPIA empresa (ej. marca
 * blanca) no tenia forma segura de saber cual empresa es "la suya" sin
 * confiar ciegamente en lo que mande el cliente en la URL.
 */
public record UsuarioAutenticado(UUID usuarioId, String identificadorEmpresa) {
}
