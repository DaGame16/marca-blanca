package com.marcablanca.platform.roles.infrastructure.web;

import com.marcablanca.platform.roles.application.port.in.GestionarRoles;
import com.marcablanca.platform.roles.domain.Rol;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
public class RolController {

    private final GestionarRoles gestionarRoles;

    public RolController(GestionarRoles gestionarRoles) {
        this.gestionarRoles = gestionarRoles;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public ResponseEntity<RolResponse> crear(@RequestBody CrearRolRequest request) {
        Rol creado = gestionarRoles.crear(request.nombre(), request.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(RolResponse.desde(creado));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('roles:leer')")
    public List<RolResponse> listar() {
        return gestionarRoles.listar().stream().map(RolResponse::desde).toList();
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('roles:leer')")
    public RolResponse consultar(@PathVariable UUID uuid) {
        return RolResponse.desde(gestionarRoles.consultarPorUuid(uuid));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public RolResponse actualizar(@PathVariable UUID uuid, @RequestBody ActualizarRolRequest request) {
        return RolResponse.desde(gestionarRoles.actualizar(uuid, request.nombre(), request.descripcion()));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID uuid) {
        gestionarRoles.eliminar(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{uuid}/permisos")
    @PreAuthorize("hasAuthority('roles:leer')")
    public List<PermisoResponse> listarPermisos(@PathVariable UUID uuid) {
        return gestionarRoles.listarPermisosDeRol(uuid).stream().map(PermisoResponse::desde).toList();
    }

    @PostMapping("/{uuid}/permisos/{permisoUuid}")
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public ResponseEntity<Void> asignarPermiso(@PathVariable UUID uuid, @PathVariable UUID permisoUuid) {
        gestionarRoles.asignarPermiso(uuid, permisoUuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{uuid}/permisos/{permisoUuid}")
    @PreAuthorize("hasAuthority('roles:gestionar')")
    public ResponseEntity<Void> quitarPermiso(@PathVariable UUID uuid, @PathVariable UUID permisoUuid) {
        gestionarRoles.quitarPermiso(uuid, permisoUuid);
        return ResponseEntity.noContent().build();
    }
}
