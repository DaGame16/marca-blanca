package com.marcablanca.platform.roles.application.port.in;

import java.util.Set;
import java.util.UUID;

/**
 * Union de los permisos de todos los roles del usuario, ajustada por sus
 * permisos puntuales (tbl_permisos_de_usuario: concede o revoca uno a uno
 * sobre lo que ya le dan sus roles). Es la puerta de entrada que consume el
 * ACL de autenticacion para armar el claim "permisos" del JWT.
 */
public interface ObtenerPermisosEfectivosDeUsuario {

    Set<String> ejecutar(UUID usuarioUuid);
}
