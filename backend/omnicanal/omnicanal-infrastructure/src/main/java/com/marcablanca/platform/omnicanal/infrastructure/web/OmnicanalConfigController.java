package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.omnicanal.application.port.in.ConfigurarOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.in.ConfigurarOmnicanal.VistaConfig;
import org.springframework.http.ResponseEntity;
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
    public VistaConfig ver() {
        return configurarOmnicanal.ver();
    }

    public record AjustesRequest(boolean iaHabilitada, String openaiModelo, String liwaBaseUrl,
                                 String liwaCustomFieldAds) {
    }

    @PutMapping
    public VistaConfig actualizarAjustes(@RequestBody AjustesRequest req) {
        configurarOmnicanal.actualizarAjustes(req.iaHabilitada(), req.openaiModelo(), req.liwaBaseUrl(),
                req.liwaCustomFieldAds());
        return configurarOmnicanal.ver();
    }

    public record TokenRequest(String token) {
    }

    @PutMapping("/liwa-token")
    public ResponseEntity<Void> definirLiwaToken(@RequestBody TokenRequest req) {
        configurarOmnicanal.definirLiwaToken(req.token());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/liwa-token")
    public ResponseEntity<Void> borrarLiwaToken() {
        configurarOmnicanal.borrarLiwaToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rotar-secreto")
    public VistaConfig rotarSecreto() {
        return configurarOmnicanal.rotarSecreto();
    }
}
