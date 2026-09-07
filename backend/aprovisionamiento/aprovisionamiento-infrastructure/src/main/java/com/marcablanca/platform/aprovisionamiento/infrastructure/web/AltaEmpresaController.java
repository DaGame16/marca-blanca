package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

import com.marcablanca.platform.aprovisionamiento.application.ComandoRegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.RegistrarEmpresa;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

/**
 * Capa 1: registra la empresa y dispara su aprovisionamiento. Responde 202
 * Accepted -- la fila queda creada de inmediato, pero la base de datos del
 * cliente se aprovisiona de forma asincrona (estado pendiente_aprovisionamiento).
 */
@RestController
@RequestMapping("/api/v1/admin/empresas")
public class AltaEmpresaController {

    private final RegistrarEmpresa registrarEmpresa;

    public AltaEmpresaController(RegistrarEmpresa registrarEmpresa) {
        this.registrarEmpresa = registrarEmpresa;
    }

    @PostMapping
    public ResponseEntity<RegistrarEmpresaResponse> registrar(@Valid @RequestBody RegistrarEmpresaRequest request) {
        UUID empresaId = registrarEmpresa.ejecutar(new ComandoRegistrarEmpresa(
                request.identificador(),
                request.nombreLegal(),
                request.nombreComercial(),
                request.dominio(),
                request.contrasenaMaestra(),
                request.modulosSolicitados() == null ? Set.of() : request.modulosSolicitados()
        ));

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .location(URI.create("/api/v1/admin/empresas/" + empresaId))
                .body(new RegistrarEmpresaResponse(empresaId, "pendiente_aprovisionamiento"));
    }
}