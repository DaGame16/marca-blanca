package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

import com.marcablanca.platform.aprovisionamiento.application.ResultadoFinalizarRegistro;
import com.marcablanca.platform.aprovisionamiento.application.port.in.FinalizarRegistro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Paso 6 del wizard publico: finalizar. Pasa la empresa a pendiente_aprovisionamiento
 * y dispara el pipeline. Responde 202 -- el aprovisionamiento sigue en segundo plano.
 */
@RestController
@RequestMapping("/api/v1/registro/empresas/{empresaId}/finalizar")
public class RegistroFinalizacionController {

    private final FinalizarRegistro finalizarRegistro;

    public RegistroFinalizacionController(FinalizarRegistro finalizarRegistro) {
        this.finalizarRegistro = finalizarRegistro;
    }

    @PostMapping
    public ResponseEntity<FinalizarRegistroResponse> finalizar(@PathVariable UUID empresaId) {
        ResultadoFinalizarRegistro r = finalizarRegistro.ejecutar(empresaId);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new FinalizarRegistroResponse(r.empresaId(), r.estado(), r.url()));
    }
}
