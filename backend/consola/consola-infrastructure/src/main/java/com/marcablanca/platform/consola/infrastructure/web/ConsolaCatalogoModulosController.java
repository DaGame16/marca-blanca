package com.marcablanca.platform.consola.infrastructure.web;

import com.marcablanca.platform.consola.application.port.out.RegistroDeAuditoria;
import com.marcablanca.platform.consola.domain.CredencialesDeOperadorInvalidasException;
import com.marcablanca.platform.modulosempresa.application.port.in.GestionarCatalogoDeModulos;
import com.marcablanca.platform.modulosempresa.application.port.in.GestionarCatalogoDeModulos.ComandoModulo;
import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulos;
import com.marcablanca.platform.modulosempresa.domain.Modulo;
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
 * CRUD del catalogo de modulos de la plataforma desde la consola. Delega en
 * `modulos-empresa`; agrega auditoria y auth de operador.
 */
@RestController
@RequestMapping("/api/v1/consola/modulos")
public class ConsolaCatalogoModulosController {

    private final ListarModulos listarModulos;
    private final GestionarCatalogoDeModulos gestionarCatalogoDeModulos;
    private final RegistroDeAuditoria registroDeAuditoria;

    public ConsolaCatalogoModulosController(ListarModulos listarModulos,
                                            GestionarCatalogoDeModulos gestionarCatalogoDeModulos,
                                            RegistroDeAuditoria registroDeAuditoria) {
        this.listarModulos = listarModulos;
        this.gestionarCatalogoDeModulos = gestionarCatalogoDeModulos;
        this.registroDeAuditoria = registroDeAuditoria;
    }

    @GetMapping
    public List<Modulo> listar() {
        return listarModulos.ejecutar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Modulo crear(@Valid @RequestBody ModuloRequest body) {
        Modulo creado = gestionarCatalogoDeModulos.crear(aComando(body));
        auditar("modulo.creado");
        return creado;
    }

    @PutMapping("/{id}")
    public Modulo actualizar(@PathVariable UUID id, @Valid @RequestBody ModuloRequest body) {
        Modulo actualizado = gestionarCatalogoDeModulos.actualizar(id, aComando(body));
        auditar("modulo.editado");
        return actualizado;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        gestionarCatalogoDeModulos.eliminar(id);
        auditar("modulo.eliminado");
    }

    private ComandoModulo aComando(ModuloRequest b) {
        return new ComandoModulo(b.codigo(), b.nombre(), b.descripcion(), b.precio(), b.moneda());
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
