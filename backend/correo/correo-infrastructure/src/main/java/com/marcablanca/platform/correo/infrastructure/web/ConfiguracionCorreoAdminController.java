package com.marcablanca.platform.correo.infrastructure.web;

import com.marcablanca.platform.correo.application.port.in.GestionarConfiguracionCorreo;
import com.marcablanca.platform.correo.application.port.in.GestionarConfiguracionCorreo.ComandoConfiguracionSmtp;
import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Protegido por X-Admin-Key (ClaveAdminInterceptor, registrado globalmente
 * para /api/v1/admin/** en el modulo modulos-empresa) -- no hace falta nada
 * aparte de vivir bajo esa ruta.
 */
@RestController
@RequestMapping("/api/v1/admin/correo/config")
public class ConfiguracionCorreoAdminController {

    private final GestionarConfiguracionCorreo gestionarConfiguracionCorreo;

    public ConfiguracionCorreoAdminController(GestionarConfiguracionCorreo gestionarConfiguracionCorreo) {
        this.gestionarConfiguracionCorreo = gestionarConfiguracionCorreo;
    }

    @PostMapping
    public ResponseEntity<ConfiguracionSmtp> crear(@RequestBody ConfiguracionCorreoRequest body) {
        ConfiguracionSmtp creada = gestionarConfiguracionCorreo.crear(aComando(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping
    public List<ConfiguracionSmtp> listar() {
        return gestionarConfiguracionCorreo.listar();
    }

    @GetMapping("/{id}")
    public ConfiguracionSmtp buscarPorId(@PathVariable UUID id) {
        return gestionarConfiguracionCorreo.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ConfiguracionSmtp actualizar(@PathVariable UUID id, @RequestBody ConfiguracionCorreoRequest body) {
        return gestionarConfiguracionCorreo.actualizar(id, aComando(body));
    }

    @PostMapping("/{id}/activar")
    public ConfiguracionSmtp activar(@PathVariable UUID id) {
        return gestionarConfiguracionCorreo.activar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        gestionarConfiguracionCorreo.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ComandoConfiguracionSmtp aComando(ConfiguracionCorreoRequest body) {
        return new ComandoConfiguracionSmtp(body.remitenteNombre(), body.remitenteCorreo(), body.responderA(),
                body.host(), body.puerto(), body.usuario(), body.secretoRef(), body.seguridad());
    }
}
