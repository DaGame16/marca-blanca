package com.marcablanca.platform.autenticacion.infrastructure.seguridad;

import com.marcablanca.platform.autenticacion.application.port.out.ConsultarPermisosDeUsuario;
import com.marcablanca.platform.roles.application.port.in.ObtenerPermisosEfectivosDeUsuario;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Unico archivo de autenticacion que conoce roles-application -- traduce
 * hacia ConsultarPermisosDeUsuario, mismo criterio que
 * AdaptadorVerificadorDeUsuarios hacia usuarios-domain.
 */
@Component
public class AdaptadorConsultarPermisosDeUsuario implements ConsultarPermisosDeUsuario {

    private final ObtenerPermisosEfectivosDeUsuario obtenerPermisosEfectivosDeUsuario;

    public AdaptadorConsultarPermisosDeUsuario(ObtenerPermisosEfectivosDeUsuario obtenerPermisosEfectivosDeUsuario) {
        this.obtenerPermisosEfectivosDeUsuario = obtenerPermisosEfectivosDeUsuario;
    }

    @Override
    public Set<String> permisosDe(UUID usuarioId) {
        return obtenerPermisosEfectivosDeUsuario.ejecutar(usuarioId);
    }
}
