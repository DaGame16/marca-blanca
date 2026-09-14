package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.omnicanal.application.port.in.ConfigurarOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.in.ConfigurarOmnicanal.VistaConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Self-service del tenant sobre su omnicanal. Protegido por el JWT normal; la
 * empresa sale del token (ContextoEmpresaActual). InterceptorModuloOmnicanal ya
 * corta con 403 si la empresa no tiene el modulo activo.
 */
@RestController
@RequestMapping("/api/v1/omnicanal/config")
public class OmnicanalConfigController {

    private final ConfigurarOmnicanal configurarOmnicanal;

    public OmnicanalConfigController(ConfigurarOmnicanal configurarOmnicanal) {
        this.configurarOmnicanal = configurarOmnicanal;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('omnicanal:configurar')")
    public VistaConfig ver() {
        return configurarOmnicanal.ver();
    }

    public record AjustesRequest(boolean iaHabilitada, String openaiModelo, String liwaBaseUrl,
                                 String liwaCustomFieldAds) {
    }

    @PutMapping
    @PreAuthorize("hasAuthority('omnicanal:configurar')")
    public VistaConfig actualizarAjustes(@RequestBody AjustesRequest req) {
        configurarOmnicanal.actualizarAjustes(req.iaHabilitada(), req.openaiModelo(), req.liwaBaseUrl(),
                req.liwaCustomFieldAds());
        return configurarOmnicanal.ver();
    }

    public record TokenRequest(String token) {
    }

    @PutMapping("/liwa-token")
    @PreAuthorize("hasAuthority('omnicanal:configurar')")
    public ResponseEntity<Void> definirLiwaToken(@RequestBody TokenRequest req) {
        configurarOmnicanal.definirLiwaToken(req.token());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/liwa-token")
    @PreAuthorize("hasAuthority('omnicanal:configurar')")
    public ResponseEntity<Void> borrarLiwaToken() {
        configurarOmnicanal.borrarLiwaToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rotar-secreto")
    @PreAuthorize("hasAuthority('omnicanal:configurar')")
    public VistaConfig rotarSecreto() {
        return configurarOmnicanal.rotarSecreto();
    }
}
