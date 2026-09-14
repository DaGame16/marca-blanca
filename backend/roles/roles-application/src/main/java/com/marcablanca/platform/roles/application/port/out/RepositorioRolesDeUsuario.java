package com.marcablanca.platform.roles.application.port.out;

import com.marcablanca.platform.roles.domain.Rol;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Pivote usuario <-> rol (tbl_usuarios_roles). Identifica al usuario por su
 * uuid -- este modulo no conoce el id interno (BIGINT) de usuarios, ni su
 * tipo de dominio; la resolucion uuid -> id interno es un detalle de la
 * implementacion en roles-infrastructure.
 */
public interface RepositorioRolesDeUsuario {

    void asignar(UUID usuarioUuid, Long rolId);

    void quitar(UUID usuarioUuid, Long rolId);

    List<Rol> listarRolesDe(UUID usuarioUuid);

    Set<Long> listarRolIdsDe(UUID usuarioUuid);
}
