package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

import com.marcablanca.platform.aprovisionamiento.application.port.in.SeleccionarModulo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Paso 2 del wizard publico: seleccion de modulos sobre la empresa en borrador. */
@RestController
@RequestMapping("/api/v1/registro/empresas/{empresaId}/modulos")
public class RegistroModulosController {

    private final SeleccionarModulo seleccionarModulo;

    public RegistroModulosController(SeleccionarModulo seleccionarModulo) {
        this.seleccionarModulo = seleccionarModulo;
    }

    @PostMapping("/{codigo}/activar")
    public ResponseEntity<Void> activar(@PathVariable UUID empresaId, @PathVariable String codigo) {
        seleccionarModulo.activar(empresaId, codigo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{codigo}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable UUID empresaId, @PathVariable String codigo) {
        seleccionarModulo.desactivar(empresaId, codigo);
        return ResponseEntity.noContent().build();
    }
}