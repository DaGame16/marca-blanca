package com.marcablanca.platform.roles.infrastructure.web;

import com.marcablanca.platform.roles.application.port.in.ConsultarPermisos;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catalogo de permisos: solo lectura, lo define el codigo de la plataforma. */
@RestController
@RequestMapping("/api/v1/permisos")
public class PermisoController {

    private final ConsultarPermisos consultarPermisos;

    public PermisoController(ConsultarPermisos consultarPermisos) {
        this.consultarPermisos = consultarPermisos;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('roles:leer')")
    public List<PermisoResponse> listar() {
        return consultarPermisos.listar().stream().map(PermisoResponse::desde).toList();
    }
}
