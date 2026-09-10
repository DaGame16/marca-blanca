package com.marcablanca.platform.omnicanal.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.marcablanca.platform.omnicanal.application.port.out.ClienteLiwa;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal.ConfiguracionDeTenant;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Unico archivo que llama a la API externa de LIWA (backfill de atribucion de
 * ads). Base, token y custom field salen de la configuracion de la empresa
 * activa (RepositorioConfiguracionOmnicanal); sin token configurado no se
 * consulta nada.
 */
@Component
class AdaptadorClienteLiwa implements ClienteLiwa {

    private final RepositorioConfiguracionOmnicanal configuracion;

    AdaptadorClienteLiwa(RepositorioConfiguracionOmnicanal configuracion) {
        this.configuracion = configuracion;
    }

    @Override
    public Optional<Boolean> consultarSiVieneDeAds(String idContacto) {
        ConfiguracionDeTenant cfg = configuracion.deLaEmpresaActiva();
        if (cfg.liwaApiToken() == null || cfg.liwaApiToken().isBlank()) {
            return Optional.empty();
        }
        try {
            RestClient restClient = RestClient.create(cfg.perfil().liwaBaseUrl());
            var respuesta = restClient.get()
                    .uri("/api/contacts/{id}/custom_fields/{campo}", idContacto, cfg.perfil().liwaCustomFieldAds())
                    .header("X-ACCESS-TOKEN", cfg.liwaApiToken())
                    .retrieve()
                    .body(JsonNode.class);

            if (respuesta.has("error")) {
                return Optional.empty();
            }
            return Optional.of("1".equals(respuesta.path("value").asText(null)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
