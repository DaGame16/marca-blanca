package com.marcablanca.platform.omnicanal.infrastructure.seguridad;

import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal.ConfiguracionDeTenant;
import com.marcablanca.platform.omnicanal.domain.Resultado;
import com.marcablanca.platform.omnicanal.domain.ResultadoAnalisisIa;
import com.marcablanca.platform.omnicanal.domain.TurnoParseado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * El unico archivo del modulo que sabe que existe OpenAI. El prompt de
 * negocio (motivos, categorias, criterio de "resuelto"/"escalado", etc.) ya
 * NO vive aca: llega en el PerfilDeAnalisisOmnicanal de la empresa activa
 * (RepositorioConfiguracionOmnicanal), con el perfil ISP por defecto para
 * quien no configuro el suyo.
 */
@Component
public class AdaptadorOpenAI implements AnalizadorDeConversacion {

    private static final Logger log = LoggerFactory.getLogger(AdaptadorOpenAI.class);

    private static final int MAX_INTENTOS = 4;
    private static final String CLAVE_CONTENT = "content";
    private static final String CAMPO_TEMAS = "temas";

    // Catalogos del prompt: si la IA devuelve algo fuera de estos, se guarda
    // null (o el default de motivo). Mismos valores que el liwa-webhook original.
    private static final String MOTIVO_POR_DEFECTO = "información";
    private static final List<String> RESULTADOS_PERMITIDOS = List.of("resuelto", "no_resuelto", "escalado");
    private static final List<String> MOTIVOS_PERMITIDOS =
            List.of("soporte", "facturación", "reconexión", "ventas", "PQR", "cobertura", MOTIVO_POR_DEFECTO);
    private static final List<String> AREAS_PERMITIDAS =
            List.of("comercial", "soporte", "facturación", "retención/PQR");
    private static final List<String> CATEGORIAS_OFICINA_PERMITIDAS = List.of(
            "CONTRATOS", "PLANES Y PROMOCIONES", "TRASLADO", "FACTURACIÓN", "RETIROS",
            "MEDIOS DE PAGO", "PAGOS Y CARTERA", "PQR", "SUCESIÓN", "REAJUSTE DEL SERVICIO");
    private static final List<String> SENTIMIENTOS_PERMITIDOS = List.of("positivo", "neutral", "negativo");
    private static final List<String> ESFUERZOS_PERMITIDOS = List.of("bajo", "medio", "alto");
    private static final List<String> TIPOS_ULTIMO_MENSAJE_EMPRESA = List.of(
            "promesa_incumplida", "pidio_datos", "respuesta_real", "despedida", "otro", "ninguno");

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

        String promptSistema = cfg.perfil().promptSistema();
        String promptUsuario = cfg.perfil().prompt(textoConversacion);

        ResultadoAnalisisIa r = parsearRespuesta(llamarConReintentos(promptSistema, promptUsuario, modelo));

        // "resultado" es la base de abandono/fcr/reportes. Si viene fuera de
        // catalogo (raro con temperature=0), vale un segundo intento antes de
        // resignarse a guardar null -- igual que en el liwa-webhook original.
        if (r.resultado() == null) {
            log.warn("La IA devolvio un 'resultado' fuera de catalogo -- reintentando una vez.");
            r = parsearRespuesta(llamarConReintentos(promptSistema, promptUsuario, modelo));
            if (r.resultado() == null) {
                log.warn("La IA volvio a devolver un 'resultado' fuera de catalogo -- se guarda como null.");
            }
        }

        return new AnalisisDeIa(r, modelo);
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
                                Map.of("role", "system", CLAVE_CONTENT, promptSistema),
                                Map.of("role", "user", CLAVE_CONTENT, promptUsuario)));

                JsonNode respuesta = restClient.post()
                        .uri("/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(cuerpo)
                        .retrieve()
                        .body(JsonNode.class);

                String contenido = respuesta == null ? null
                        : respuesta.path("choices").path(0).path("message").path(CLAVE_CONTENT).asString(null);
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
                esperar(esperaMs(e, intento));
            }
        }
        throw ultimoError;
    }

    /** Respeta el header Retry-After si viene; si no, backoff exponencial con jitter. */
    private static long esperaMs(org.springframework.web.client.HttpStatusCodeException e, int intento) {
        HttpHeaders headers = e.getResponseHeaders();
        String retryAfter = headers == null ? null : headers.getFirst("Retry-After");
        if (retryAfter != null) {
            try {
                long segundos = Long.parseLong(retryAfter.trim());
                if (segundos > 0) {
                    return Math.min(30000, segundos * 1000);
                }
            } catch (NumberFormatException _) {
                // Retry-After tambien puede venir como fecha HTTP; en ese caso se ignora
                // y se cae al backoff exponencial.
            }
        }
        long base = Math.min(30000, 1000L * (1L << (intento - 1)));
        return base + java.util.concurrent.ThreadLocalRandom.current().nextInt(300);
    }

    private void esperar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    ResultadoAnalisisIa parsearRespuesta(String contenidoJson) {
        try {
            JsonNode n = mapper.readTree(contenidoJson);

            String resultadoTexto = sanitizarEnum(textoONull(n, "resultado"), RESULTADOS_PERMITIDOS);
            Resultado resultado = resultadoTexto == null ? null : Resultado.valueOf(resultadoTexto.toUpperCase());

            String motivo = sanitizarEnum(textoONull(n, "motivo_contacto"), MOTIVOS_PERMITIDOS);

            List<String> temas = new ArrayList<>();
            if (n.has(CAMPO_TEMAS) && n.get(CAMPO_TEMAS).isArray()) {
                n.get(CAMPO_TEMAS).forEach(t -> temas.add(t.asString()));
            }

            return new ResultadoAnalisisIa(
                    textoONull(n, "razonamiento"),
                    motivo != null ? motivo : MOTIVO_POR_DEFECTO,
                    textoONull(n, "submotivo"),
                    sanitizarEnum(textoONull(n, "categoria_oficina"), CATEGORIAS_OFICINA_PERMITIDAS),
                    textoONull(n, "municipio"), textoONull(n, "barrio"),
                    sanitizarEnum(textoONull(n, "area_destino"), AREAS_PERMITIDAS),
                    textoONull(n, "resumen_motivo"), textoONull(n, "resumen_desenlace"),
                    sanitizarEnum(textoONull(n, "sentimiento_inicial"), SENTIMIENTOS_PERMITIDOS),
                    sanitizarEnum(textoONull(n, "sentimiento_final"), SENTIMIENTOS_PERMITIDOS),
                    resultado,
                    sanitizarEnum(textoONull(n, "tipo_ultimo_mensaje_empresa"), TIPOS_ULTIMO_MENSAJE_EMPRESA),
                    boolONull(n, "fcr"),
                    sanitizarEnum(textoONull(n, "esfuerzo_cliente"), ESFUERZOS_PERMITIDOS),
                    temas.stream().limit(6).toList(), boolONull(n, "oportunidad_venta"),
                    boolONull(n, "venta_confirmada_en_texto"), boolONull(n, "trato_inadecuado"),
                    n.path("gestion_pendiente").asBoolean(false), n.path("revisar_limite").asBoolean(false));
        } catch (RuntimeException ex) {
            throw new IllegalStateException("No se pudo parsear la respuesta de OpenAI: " + ex.getMessage(), ex);
        }
    }

    /**
     * Devuelve el valor CANONICO del catalogo (respetando su capitalizacion)
     * si el texto de la IA coincide sin importar mayusculas/espacios; null si
     * no esta en el catalogo. Igual que sanitizarEnum del liwa-webhook.
     */
    static String sanitizarEnum(String valor, List<String> permitidos) {
        if (valor == null) {
            return null;
        }
        String t = valor.trim().toLowerCase();
        if (t.isEmpty()) {
            return null;
        }
        for (String p : permitidos) {
            if (p.toLowerCase().equals(t)) {
                return p;
            }
        }
        return null;
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
