package com.marcablanca.platform.roles.infrastructure.web;

import com.marcablanca.platform.roles.application.port.in.GestionarAsignacionesDeUsuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Anidado bajo /api/v1/usuarios/{uuid} por legibilidad del contrato REST,
 * pero es parte del modulo roles (no de usuarios) -- gestiona los pivotes
 * usuario<->rol y los ajustes puntuales de permiso.
 */
@RestController
@RequestMapping("/api/v1/usuarios/{usuarioUuid}")
public class AsignacionUsuarioRolController {

    private final GestionarAsignacionesDeUsuario gestionarAsignaciones;

    public AsignacionUsuarioRolController(GestionarAsignacionesDeUsuario gestionarAsignaciones) {
        this.gestionarAsignaciones = gestionarAsignaciones;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('roles:leer')")
    public List<RolResponse> listarRoles(@PathVariable UUID usuarioUuid) {
        return gestionarAsignaciones.listarRolesDeUsuario(usuarioUuid).stream().map(RolResponse::desde).toList();
    }

    @PostMapping("/roles/{rolUuid}")
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public ResponseEntity<Void> asignarRol(@PathVariable UUID usuarioUuid, @PathVariable UUID rolUuid) {
        gestionarAsignaciones.asignarRol(usuarioUuid, rolUuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/roles/{rolUuid}")
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public ResponseEntity<Void> quitarRol(@PathVariable UUID usuarioUuid, @PathVariable UUID rolUuid) {
        gestionarAsignaciones.quitarRol(usuarioUuid, rolUuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permisos")
    @PreAuthorize("hasAuthority('roles:leer')")
    public Map<String, Boolean> listarAjustesDePermisos(@PathVariable UUID usuarioUuid) {
        return gestionarAsignaciones.listarAjustesDeUsuario(usuarioUuid);
    }

    @PostMapping("/permisos/{permisoUuid}/conceder")
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public ResponseEntity<Void> concederPermiso(@PathVariable UUID usuarioUuid, @PathVariable UUID permisoUuid) {
        gestionarAsignaciones.concederPermiso(usuarioUuid, permisoUuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/permisos/{permisoUuid}/revocar")
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public ResponseEntity<Void> revocarPermiso(@PathVariable UUID usuarioUuid, @PathVariable UUID permisoUuid) {
        gestionarAsignaciones.revocarPermiso(usuarioUuid, permisoUuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/permisos/{permisoUuid}")
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public ResponseEntity<Void> quitarAjustePermiso(@PathVariable UUID usuarioUuid, @PathVariable UUID permisoUuid) {
        gestionarAsignaciones.quitarAjustePermiso(usuarioUuid, permisoUuid);
        return ResponseEntity.noContent().build();
    }
}
