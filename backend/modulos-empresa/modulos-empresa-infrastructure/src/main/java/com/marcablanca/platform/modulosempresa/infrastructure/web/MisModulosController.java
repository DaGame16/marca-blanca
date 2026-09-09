package com.marcablanca.platform.modulosempresa.infrastructure.web;

import com.marcablanca.platform.empresas.application.port.in.ResolverUuidDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.ActivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.DesactivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.domain.ModuloDeEmpresa;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Self-service -- protegido por el JWT normal (no X-Admin-Key). La empresa
 * se saca del token ya verificado, igual que en identidad-visual/MarcaController,
 * nunca de un parametro que mande el cliente. Los casos de uso que reutiliza
 * (Listar/Activar/DesactivarModuloDeEmpresa) ya existian para el panel de
 * administracion de plataforma y trabajan con UUID interno; ResolverUuidDeEmpresa
 * (modulo empresas) hace el puente desde el identificador del token.
 */
@RestController
@RequestMapping("/api/v1/mi-empresa/modulos")
public class MisModulosController {

    private static final String ATRIBUTO_EMPRESA = "identificadorEmpresa";

    private final ResolverUuidDeEmpresa resolverUuidDeEmpresa;
    private final ListarModulosDeEmpresa listarModulosDeEmpresa;
    private final ActivarModuloDeEmpresa activarModuloDeEmpresa;
    private final DesactivarModuloDeEmpresa desactivarModuloDeEmpresa;

    public MisModulosController(ResolverUuidDeEmpresa resolverUuidDeEmpresa,
                                 ListarModulosDeEmpresa listarModulosDeEmpresa,
                                 ActivarModuloDeEmpresa activarModuloDeEmpresa,
                                 DesactivarModuloDeEmpresa desactivarModuloDeEmpresa) {
        this.resolverUuidDeEmpresa = resolverUuidDeEmpresa;
        this.listarModulosDeEmpresa = listarModulosDeEmpresa;
        this.activarModuloDeEmpresa = activarModuloDeEmpresa;
        this.desactivarModuloDeEmpresa = desactivarModuloDeEmpresa;
    }

    @GetMapping
    public List<ModuloDeEmpresa> listar(HttpServletRequest request) {
        return listarModulosDeEmpresa.ejecutar(empresaIdDelToken(request));
    }

    @PostMapping("/{codigo}/activar")
    public ResponseEntity<Void> activar(HttpServletRequest request, @PathVariable String codigo) {
        activarModuloDeEmpresa.ejecutar(empresaIdDelToken(request), codigo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codigo}/desactivar")
    public ResponseEntity<Void> desactivar(HttpServletRequest request, @PathVariable String codigo) {
        desactivarModuloDeEmpresa.ejecutar(empresaIdDelToken(request), codigo);
        return ResponseEntity.noContent().build();
    }

    private UUID empresaIdDelToken(HttpServletRequest request) {
        String identificadorEmpresa = (String) request.getAttribute(ATRIBUTO_EMPRESA);
        if (identificadorEmpresa == null) {
            throw new IllegalStateException(
                    "No hay empresa en el token -- endpoint mal protegido o JWT sin el claim 'empresa'.");
        }
        return resolverUuidDeEmpresa.ejecutar(identificadorEmpresa);
    }
}
