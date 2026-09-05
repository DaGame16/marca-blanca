package com.marcablanca.platform.usuarios.infrastructure.web;

import com.marcablanca.platform.usuarios.application.port.in.GestionarUsuario;
import com.marcablanca.platform.usuarios.domain.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UsuarioResponse> crear(@RequestBody CrearUsuarioRequest request) {
        Usuario creado = gestionarUsuario.crear(request.correo(), request.contrasena(), request.nombreCompleto());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(creado));
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return gestionarUsuario.listar().stream().map(UsuarioResponse::desde).toList();
    }

    @GetMapping("/{uuid}")
    public UsuarioResponse consultar(@PathVariable UUID uuid) {
        return UsuarioResponse.desde(gestionarUsuario.consultarPorUuid(uuid));
    }

    @PutMapping("/{uuid}")
    public UsuarioResponse actualizar(@PathVariable UUID uuid, @RequestBody ActualizarUsuarioRequest request) {
        return UsuarioResponse.desde(gestionarUsuario.actualizar(uuid, request.nombreCompleto()));
    }

    @PutMapping("/{uuid}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable UUID uuid) {
        gestionarUsuario.desactivar(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}/activar")
    public ResponseEntity<Void> activar(@PathVariable UUID uuid) {
        gestionarUsuario.activar(uuid);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}/perfil")
    public void actualizarPerfil(@PathVariable UUID uuid, @RequestBody ActualizarPerfilRequest request) {
        gestionarUsuario.actualizarPerfil(uuid, request.cedula(), request.tipoDocumento(),
                request.telefono(), request.direccion(),
                request.contactoEmergencia(), request.telefonoEmergencia());
    }
}