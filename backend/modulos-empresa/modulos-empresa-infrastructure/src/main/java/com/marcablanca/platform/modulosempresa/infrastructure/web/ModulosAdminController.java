package com.marcablanca.platform.modulosempresa.infrastructure.web;

import com.marcablanca.platform.modulosempresa.application.port.in.ActivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.DesactivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulos;
import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.domain.Modulo;
import com.marcablanca.platform.modulosempresa.domain.ModuloDeEmpresa;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class ModulosAdminController {

    private final ListarModulos listarModulos;
    private final ListarModulosDeEmpresa listarModulosDeEmpresa;
    private final ActivarModuloDeEmpresa activarModuloDeEmpresa;
    private final DesactivarModuloDeEmpresa desactivarModuloDeEmpresa;

    public ModulosAdminController(ListarModulos listarModulos,
                                   ListarModulosDeEmpresa listarModulosDeEmpresa,
                                   ActivarModuloDeEmpresa activarModuloDeEmpresa,
                                   DesactivarModuloDeEmpresa desactivarModuloDeEmpresa) {
        this.listarModulos = listarModulos;
        this.listarModulosDeEmpresa = listarModulosDeEmpresa;
        this.activarModuloDeEmpresa = activarModuloDeEmpresa;
        this.desactivarModuloDeEmpresa = desactivarModuloDeEmpresa;
    }

    @GetMapping("/modulos")
    public List<Modulo> listarCatalogo() {
        return listarModulos.ejecutar();
    }

    @GetMapping("/empresas/{empresaId}/modulos")
    public List<ModuloDeEmpresa> listarModulosDeEmpresa(@PathVariable UUID empresaId) {
        return listarModulosDeEmpresa.ejecutar(empresaId);
    }

    @PostMapping("/empresas/{empresaId}/modulos/{codigo}/activar")
    public ResponseEntity<Void> activar(@PathVariable UUID empresaId, @PathVariable String codigo) {
        activarModuloDeEmpresa.ejecutar(empresaId, codigo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/empresas/{empresaId}/modulos/{codigo}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable UUID empresaId, @PathVariable String codigo) {
        desactivarModuloDeEmpresa.ejecutar(empresaId, codigo);
        return ResponseEntity.noContent().build();
    }
}
