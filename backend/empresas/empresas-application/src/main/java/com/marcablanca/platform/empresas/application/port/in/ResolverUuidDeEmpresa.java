package com.marcablanca.platform.empresas.application.port.in;

import java.util.UUID;

/**
 * Puente para endpoints self-service (protegidos por JWT normal, no
 * X-Admin-Key) que necesitan el UUID interno de la empresa a partir del
 * identificador que trae el token -- ej. modulos-empresa, cuyos casos de
 * uso ya existentes trabajan con UUID porque los pensaron para el panel de
 * administracion de plataforma.
 */
public interface ResolverUuidDeEmpresa {
    UUID ejecutar(String identificadorEmpresa);
}
