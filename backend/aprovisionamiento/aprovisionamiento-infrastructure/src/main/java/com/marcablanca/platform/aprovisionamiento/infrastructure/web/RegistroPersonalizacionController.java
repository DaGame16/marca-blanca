package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

import com.marcablanca.platform.aprovisionamiento.application.port.in.PersonalizarEmpresa;
import com.marcablanca.platform.aprovisionamiento.domain.ColorHex;
import com.marcablanca.platform.aprovisionamiento.domain.Personalizacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Pasos 3-5 del wizard publico: colores, logo y variantes de UI sobre la empresa en borrador. */
@RestController
@RequestMapping("/api/v1/registro/empresas/{empresaId}/personalizacion")
public class RegistroPersonalizacionController {

    private final PersonalizarEmpresa personalizarEmpresa;

    public RegistroPersonalizacionController(PersonalizarEmpresa personalizarEmpresa) {
        this.personalizarEmpresa = personalizarEmpresa;
    }

    @PutMapping
    public ResponseEntity<Void> personalizar(@PathVariable UUID empresaId, @RequestBody PersonalizacionRequest body) {
        Personalizacion p = new Personalizacion(
                body.colorPrimario() == null ? null : new ColorHex(body.colorPrimario()),
                body.colorSecundario() == null ? null : new ColorHex(body.colorSecundario()),
                body.urlLogo(),
                body.tipoLogin() == null ? 1 : body.tipoLogin(),
                body.tipoPantallaPrincipal() == null ? 1 : body.tipoPantallaPrincipal());
        personalizarEmpresa.ejecutar(empresaId, p);
        return ResponseEntity.noContent().build();
    }
}
