package com.marcablanca.platform.identidadvisual.infrastructure.web;

import com.marcablanca.platform.identidadvisual.application.port.in.ObtenerMarcaDeEmpresa;
import com.marcablanca.platform.identidadvisual.domain.MarcaDeEmpresa;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publico (permitAll en SecurityConfig) -- la pantalla de login todavia no
 * tiene JWT cuando necesita pintar el logo/colores/variante de la empresa a
 * la que pertenece el subdominio. No expone nada sensible (logo, 2 colores,
 * 2 codigos de variante de UI), solo lo necesario para pintar esa pantalla
 * antes de autenticarse.
 */
@RestController
@RequestMapping("/api/v1/empresas/{identificadorEmpresa}/marca")
public class MarcaPublicaController {

    private final ObtenerMarcaDeEmpresa obtenerMarcaDeEmpresa;

    public MarcaPublicaController(ObtenerMarcaDeEmpresa obtenerMarcaDeEmpresa) {
        this.obtenerMarcaDeEmpresa = obtenerMarcaDeEmpresa;
    }

    @GetMapping
    public MarcaResponse obtener(@PathVariable String identificadorEmpresa) {
        MarcaDeEmpresa marca = obtenerMarcaDeEmpresa.ejecutar(identificadorEmpresa);
        return new MarcaResponse(
                marca.urlLogo(),
                marca.colorPrimario() != null ? marca.colorPrimario().valor() : null,
                marca.colorSecundario() != null ? marca.colorSecundario().valor() : null,
                marca.dominioPropio(),
                marca.tipoLogin(),
                marca.tipoPantallaPrincipal()
        );
    }
}
