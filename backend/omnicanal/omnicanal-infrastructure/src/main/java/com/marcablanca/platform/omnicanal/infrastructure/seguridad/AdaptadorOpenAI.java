package com.marcablanca.platform.omnicanal.infrastructure.seguridad;

import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal.ConfiguracionDeTenant;
import com.marcablanca.platform.omnicanal.domain.Resultado;
import com.marcablanca.platform.omnicanal.domain.ResultadoAnalisisIa;
import com.marcablanca.platform.omnicanal.domain.TurnoParseado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Unico archivo de todo el modulo que sabe que existe OpenAI. El prompt de
 * negocio (motivos, categorias, criterio de "resuelto"/"escalado", etc.) ya
 * NO vive aca: llega en el PerfilDeAnalisisOmnicanal de la empresa activa
 * (RepositorioConfiguracionOmnicanal), con el perfil ISP por defecto para
 * quien no configuro el suyo.
 */
@Component
public class AdaptadorOpenAI implements AnalizadorDeConversacion {

    private static final int MAX_INTENTOS = 4;
    private static final List<String> RESULTADOS_PERMITIDOS = List.of("resuelto", "no_resuelto", "escalado");

    private final RestClient restClient = RestClient.create("https://api.openai.com/v1");

    private final ObjectMapper mapper;
    private final String apiKey;
    private final String modeloPorDefecto;
    private final RepositorioConfiguracionOmnicanal configuracion;

    public AdaptadorOpenAI(@Value("${app.omnicanal.openai-api-key:}") String apiKey,
                           @Value("${app.omnicanal.openai-modelo:gpt-4.1-mini}") String modeloPorDefecto,
                           RepositorioConfiguracionOmnicanal configuracion,
                           ObjectMapper mapper) {
        this.apiKey = apiKey;
        this.modeloPorDefecto = modeloPorDefecto;
        this.configuracion = configuracion;
        this.mapper = mapper;
    }

    @Override
    public AnalisisDeIa analizar(List<TurnoParseado> turnosRelevantes) {
        ConfiguracionDeTenant cfg = configuracion.deLaEmpresaActiva();

        if (!cfg.iaHabilitada() || apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Analisis IA deshabilitado para la empresa o sin OPENAI_API_KEY -- ver "
                            + "omnicanal.tbl_configuracion_omnicanal.ia_habilitada y app.omnicanal.openai-api-key.");
        }

        String modelo = (cfg.openaiModelo() != null && !cfg.openaiModelo().isBlank())
                ? cfg.openaiModelo()
                : modeloPorDefecto;

        String textoConversacion = turnosRelevantes.stream()
                .map(t -> "[" + t.autor().name() + "] " + t.nombre() + " (" + t.fechaTexto() + "): " + t.mensaje())
                .reduce((a, b) -> a + "\n\n" + b).orElse("");

        String contenido = llamarConReintentos(cfg.perfil().promptSistema(),
                cfg.perfil().prompt(textoConversacion), modelo);
        return new AnalisisDeIa(parsearRespuesta(contenido), modelo);
    }

    private String llamarConReintentos(String promptSistema, String promptUsuario, String modelo) {
        RuntimeException ultimoError = new IllegalStateException("OpenAI: sin intentos realizados");

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try {
                Map<String, Object> cuerpo = Map.of(
                        "model", modelo,
                        "response_format", Map.of("type", "json_object"),
                        "temperature", 0,
                        "messages", List.of(
                                Map.of("role", "system", "content", promptSistema),
                                Map.of("role", "user", "content", promptUsuario)));

                JsonNode respuesta = restClient.post()
                        .uri("/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(cuerpo)
                        .retrieve()
                        .body(JsonNode.class);

                String contenido = respuesta.path("choices").path(0).path("message").path("content").asString(null);
                if (contenido == null) {
                    throw new IllegalStateException("Respuesta de OpenAI sin contenido");
                }
                return contenido;
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                boolean reintentable = e.getStatusCode().value() == 429 || e.getStatusCode().is5xxServerError();
                ultimoError = new IllegalStateException("OpenAI respondio " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
                if (!reintentable || intento == MAX_INTENTOS) {
                    throw ultimoError;
                }
                esperar(Math.min(30000, 1000L * (1L << (intento - 1))));
            }
        }
        throw ultimoError;
    }

    private void esperar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ResultadoAnalisisIa parsearRespuesta(String contenidoJson) {
        try {
            JsonNode n = mapper.readTree(contenidoJson);
            String resultadoTexto = n.path("resultado").asString(null);
            Resultado resultado = null;
            if (resultadoTexto != null && RESULTADOS_PERMITIDOS.contains(resultadoTexto.toLowerCase())) {
                resultado = Resultado.valueOf(resultadoTexto.toUpperCase());
            }

            List<String> temas = new ArrayList<>();
            if (n.has("temas") && n.get("temas").isArray()) {
                n.get("temas").forEach(t -> temas.add(t.asString()));
            }

            return new ResultadoAnalisisIa(
                    textoONull(n, "razonamiento"), textoONull(n, "motivo_contacto"), textoONull(n, "submotivo"),
                    textoONull(n, "categoria_oficina"), textoONull(n, "municipio"), textoONull(n, "barrio"),
                    textoONull(n, "area_destino"), textoONull(n, "resumen_motivo"), textoONull(n, "resumen_desenlace"),
                    textoONull(n, "sentimiento_inicial"), textoONull(n, "sentimiento_final"), resultado,
                    textoONull(n, "tipo_ultimo_mensaje_empresa"), boolONull(n, "fcr"), textoONull(n, "esfuerzo_cliente"),
                    temas.stream().limit(6).toList(), boolONull(n, "oportunidad_venta"),
                    boolONull(n, "venta_confirmada_en_texto"), boolONull(n, "trato_inadecuado"),
                    n.path("gestion_pendiente").asBoolean(false), n.path("revisar_limite").asBoolean(false));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo parsear la respuesta de OpenAI: " + e.getMessage(), e);
        }
    }

    private String textoONull(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        return (v == null || v.isNull()) ? null : v.asString();
    }

    private Boolean boolONull(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        return (v == null || v.isNull() || !v.isBoolean()) ? null : v.asBoolean();
    }
}
