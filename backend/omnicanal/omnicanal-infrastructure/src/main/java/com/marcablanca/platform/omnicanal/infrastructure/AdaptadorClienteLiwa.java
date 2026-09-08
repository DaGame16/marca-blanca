package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.application.port.out.ClienteLiwa;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/** Unico archivo que llama a la API externa de chat.liwa.co (backfill de atribucion de ads). */
@Component
class AdaptadorClienteLiwa implements ClienteLiwa {

    private static final String CUSTOM_FIELD_ID = "587226"; // VienePautasMeta
    private final RestClient restClient = RestClient.create("https://chat.liwa.co");

    @Override
    public Optional<Boolean> consultarSiVieneDeAds(String idContacto, String apiToken) {
        try {
            var respuesta = restClient.get()
                    .uri("/api/contacts/{id}/custom_fields/{campo}", idContacto, CUSTOM_FIELD_ID)
                    .header("X-ACCESS-TOKEN", apiToken)
                    .retrieve()
                    .body(com.fasterxml.jackson.databind.JsonNode.class);

            if (respuesta.has("error")) {
                return Optional.empty();
            }
            return Optional.of("1".equals(respuesta.path("value").asText(null)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
