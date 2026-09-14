package com.marcablanca.platform.autenticacion.application.port.out;

import java.util.Set;
import java.util.UUID;

/**
 * Puerto propio de autenticacion para consultar los permisos efectivos de un
 * usuario -- nunca se importa nada de roles-application/domain fuera de la
 * implementacion de este puerto (AdaptadorConsultarPermisosDeUsuario). Mismo
 * criterio que VerificadorDeUsuarios hacia usuarios-domain, pero separado:
 * identidad y autorizacion son ACLs distintos hacia modulos distintos.
 */
public interface ConsultarPermisosDeUsuario {

    Set<String> permisosDe(UUID usuarioId);
}
