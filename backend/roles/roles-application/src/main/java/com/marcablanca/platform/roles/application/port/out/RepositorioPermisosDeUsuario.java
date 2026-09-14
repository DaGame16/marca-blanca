package com.marcablanca.platform.roles.application.port.out;

import java.util.Map;
import java.util.UUID;

/**
 * Ajustes puntuales de permiso por usuario (tbl_permisos_de_usuario):
 * es_concedido=true concede un permiso que sus roles no le dan, false
 * revoca uno que sus roles si le darian.
 */
public interface RepositorioPermisosDeUsuario {

    void registrarAjuste(UUID usuarioUuid, Long permisoId, boolean esConcedido);

    void eliminarAjuste(UUID usuarioUuid, Long permisoId);

    /** Clave = nombre del permiso, valor = es_concedido. */
    Map<String, Boolean> listarAjustesDe(UUID usuarioUuid);
}
