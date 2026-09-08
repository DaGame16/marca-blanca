package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

import com.marcablanca.platform.aprovisionamiento.application.ComandoRegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.ResultadoRegistroEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.RegistrarEmpresa;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Paso 1 del registro de empresa. Endpoint PUBLICO (sin JWT ni X-Admin-Key): un
 * prospecto entra a la plataforma y se registra solo. Crea la empresa en estado
 * 'borrador'; los pasos siguientes (modulos, marca, variantes) la completan.
 */
@RestController
@RequestMapping("/api/v1/registro/empresas")
public class AltaEmpresaController {

    private final RegistrarEmpresa registrarEmpresa;

    public AltaEmpresaController(RegistrarEmpresa registrarEmpresa) {
        this.registrarEmpresa = registrarEmpresa;
    }

    @PostMapping
    public ResponseEntity<RegistrarEmpresaResponse> registrar(@Valid @RequestBody RegistrarEmpresaRequest request) {
        ResultadoRegistroEmpresa r = registrarEmpresa.ejecutar(new ComandoRegistrarEmpresa(
                request.nombreEmpresa(),
                request.representanteLegal(),
                request.correo(),
                request.telefono(),
                request.sitioWeb()));

        return ResponseEntity
                .created(URI.create("/api/v1/registro/empresas/" + r.empresaId()))
                .body(new RegistrarEmpresaResponse(r.empresaId(), r.identificador(), r.dominio(), r.estado()));
    }
}
