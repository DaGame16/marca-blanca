package com.marcablanca.platform.consola.infrastructure.web;

import com.marcablanca.platform.consola.application.port.out.RegistroDeAuditoria;
import com.marcablanca.platform.consola.domain.CredencialesDeOperadorInvalidasException;
import com.marcablanca.platform.correo.application.port.in.GestionarConfiguracionCorreo;
import com.marcablanca.platform.correo.application.port.in.GestionarConfiguracionCorreo.ComandoConfiguracionSmtp;
import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Configuracion SMTP de la plataforma desde la consola de operacion. Delega en el
 * caso de uso del modulo `correo`; agrega auditoria y auth de operador. La clave
 * SMTP no pasa por aca -- {@code secretoRef} apunta a donde vive (env/vault).
 */
@RestController
@RequestMapping("/api/v1/consola/config-correo")
public class ConsolaConfigCorreoController {

    private final GestionarConfiguracionCorreo gestionarConfiguracionCorreo;
    private final RegistroDeAuditoria registroDeAuditoria;

    public ConsolaConfigCorreoController(GestionarConfiguracionCorreo gestionarConfiguracionCorreo,
                                        RegistroDeAuditoria registroDeAuditoria) {
        this.gestionarConfiguracionCorreo = gestionarConfiguracionCorreo;
        this.registroDeAuditoria = registroDeAuditoria;
    }

    @GetMapping
    public List<ConfiguracionSmtp> listar() {
        return gestionarConfiguracionCorreo.listar();
    }

    @GetMapping("/{id}")
    public ConfiguracionSmtp buscar(@PathVariable UUID id) {
        return gestionarConfiguracionCorreo.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConfiguracionSmtp crear(@Valid @RequestBody ConfigCorreoRequest body) {
        ConfiguracionSmtp creada = gestionarConfiguracionCorreo.crear(aComando(body));
        auditar("correo.config_creada");
        return creada;
    }

    @PutMapping("/{id}")
    public ConfiguracionSmtp actualizar(@PathVariable UUID id, @Valid @RequestBody ConfigCorreoRequest body) {
        ConfiguracionSmtp actualizada = gestionarConfiguracionCorreo.actualizar(id, aComando(body));
        auditar("correo.config_editada");
        return actualizada;
    }

    @PostMapping("/{id}/activar")
    public ConfiguracionSmtp activar(@PathVariable UUID id) {
        ConfiguracionSmtp activada = gestionarConfiguracionCorreo.activar(id);
        auditar("correo.config_activada");
        return activada;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        gestionarConfiguracionCorreo.eliminar(id);
        auditar("correo.config_eliminada");
    }

    private ComandoConfiguracionSmtp aComando(ConfigCorreoRequest b) {
        return new ComandoConfiguracionSmtp(b.remitenteNombre(), b.remitenteCorreo(), b.responderA(),
                b.host(), b.puerto(), b.usuario(), b.secretoRef(), b.seguridad());
    }

    private void auditar(String accion) {
        registroDeAuditoria.registrar(operadorAutenticadoId(), accion, null);
    }

    private UUID operadorAutenticadoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UUID id) {
            return id;
        }
        throw new CredencialesDeOperadorInvalidasException();
    }
}
