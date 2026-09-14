package com.marcablanca.platform.roles.infrastructure;

import com.marcablanca.platform.roles.application.CalcularPermisosEfectivosService;
import com.marcablanca.platform.roles.application.ConsultarPermisosService;
import com.marcablanca.platform.roles.application.GestionarAsignacionesDeUsuarioService;
import com.marcablanca.platform.roles.application.GestionarRolesService;
import com.marcablanca.platform.roles.application.port.in.ConsultarPermisos;
import com.marcablanca.platform.roles.application.port.in.GestionarAsignacionesDeUsuario;
import com.marcablanca.platform.roles.application.port.in.GestionarRoles;
import com.marcablanca.platform.roles.application.port.in.ObtenerPermisosEfectivosDeUsuario;
import com.marcablanca.platform.roles.application.port.out.RepositorioPermisos;
import com.marcablanca.platform.roles.application.port.out.RepositorioPermisosDeUsuario;
import com.marcablanca.platform.roles.application.port.out.RepositorioRoles;
import com.marcablanca.platform.roles.application.port.out.RepositorioRolesDeUsuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionRoles {

    @Bean
    public GestionarRoles gestionarRoles(RepositorioRoles repositorioRoles, RepositorioPermisos repositorioPermisos) {
        return new GestionarRolesService(repositorioRoles, repositorioPermisos);
    }

    @Bean
    public ConsultarPermisos consultarPermisos(RepositorioPermisos repositorioPermisos) {
        return new ConsultarPermisosService(repositorioPermisos);
    }

    @Bean
    public GestionarAsignacionesDeUsuario gestionarAsignacionesDeUsuario(
            RepositorioRolesDeUsuario repositorioRolesDeUsuario,
            RepositorioPermisosDeUsuario repositorioPermisosDeUsuario,
            RepositorioRoles repositorioRoles,
            RepositorioPermisos repositorioPermisos) {
        return new GestionarAsignacionesDeUsuarioService(
                repositorioRolesDeUsuario, repositorioPermisosDeUsuario, repositorioRoles, repositorioPermisos);
    }

    @Bean
    public ObtenerPermisosEfectivosDeUsuario obtenerPermisosEfectivosDeUsuario(
            RepositorioRolesDeUsuario repositorioRolesDeUsuario,
            RepositorioPermisos repositorioPermisos,
            RepositorioPermisosDeUsuario repositorioPermisosDeUsuario) {
        return new CalcularPermisosEfectivosService(
                repositorioRolesDeUsuario, repositorioPermisos, repositorioPermisosDeUsuario);
    }
}
