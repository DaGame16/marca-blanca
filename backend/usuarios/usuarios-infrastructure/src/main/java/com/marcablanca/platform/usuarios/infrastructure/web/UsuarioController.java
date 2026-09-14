package com.marcablanca.platform.usuarios.infrastructure.web;

import com.marcablanca.platform.usuarios.application.port.in.GestionarUsuario;
import com.marcablanca.platform.usuarios.domain.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final GestionarUsuario gestionarUsuario;

    public UsuarioController(GestionarUsuario gestionarUsuario) {
        this.gestionarUsuario = gestionarUsuario;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('usuarios:crear')")
    public ResponseEntity<UsuarioResponse> crear(@RequestBody CrearUsuarioRequest request) {
        Usuario creado = gestionarUsuario.crear(request.correo(), request.contrasena(), request.nombreCompleto());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(creado));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('usuarios:leer')")
    public List<UsuarioResponse> listar() {
        return gestionarUsuario.listar().stream().map(UsuarioResponse::desde).toList();
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('usuarios:leer')")
    public UsuarioResponse consultar(@PathVariable UUID uuid) {
        return UsuarioResponse.desde(gestionarUsuario.consultarPorUuid(uuid));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('usuarios:editar')")
    public UsuarioResponse actualizar(@PathVariable UUID uuid, @RequestBody ActualizarUsuarioRequest request) {
        return UsuarioResponse.desde(gestionarUsuario.actualizar(uuid, request.nombreCompleto()));
    }

    @PutMapping("/{uuid}/desactivar")
    @PreAuthorize("hasAuthority('usuarios:desactivar')")
    public ResponseEntity<Void> desactivar(@PathVariable UUID uuid) {
        gestionarUsuario.desactivar(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}/activar")
    @PreAuthorize("hasAuthority('usuarios:activar')")
    public ResponseEntity<Void> activar(@PathVariable UUID uuid) {
        gestionarUsuario.activar(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}/perfil")
    @PreAuthorize("hasAuthority('usuarios:editar')")
    public void actualizarPerfil(@PathVariable UUID uuid, @RequestBody ActualizarPerfilRequest request) {
        gestionarUsuario.actualizarPerfil(uuid, request.cedula(), request.tipoDocumento(),
                request.telefono(), request.direccion(),
                request.contactoEmergencia(), request.telefonoEmergencia());
    }
}