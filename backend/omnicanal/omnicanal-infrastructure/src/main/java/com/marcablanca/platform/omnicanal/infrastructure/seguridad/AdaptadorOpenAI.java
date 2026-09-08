package com.marcablanca.platform.omnicanal.infrastructure.seguridad;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.domain.Resultado;
import com.marcablanca.platform.omnicanal.domain.ResultadoAnalisisIa;
import com.marcablanca.platform.omnicanal.domain.TurnoParseado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Unico archivo de todo el modulo que sabe que existe OpenAI. El prompt de
 * negocio (motivos, categorias, criterio de "resuelto"/"escalado", etc.) es
 * el mismo, tal cual, del pipeline anterior -- es conocimiento de negocio
 * afinado con casos reales, no algo que se deba reinventar en la migracion.
 */
@Component
public class AdaptadorOpenAI implements AnalizadorDeConversacion {

    private static final int MAX_INTENTOS = 4;
    private static final List<String> RESULTADOS_PERMITIDOS = List.of("resuelto", "no_resuelto", "escalado");

    private final RestClient restClient = RestClient.create("https://api.openai.com/v1");
    private final ObjectMapper mapper = new ObjectMapper();

    private final String apiKey;
    private final String modelo;
    private final boolean habilitado;

    public AdaptadorOpenAI(@Value("${app.omnicanal.openai-api-key:}") String apiKey,
                            @Value("${app.omnicanal.openai-modelo:gpt-4.1-mini}") String modelo,
                            @Value("${app.omnicanal.ia-habilitada:false}") boolean habilitado) {
        this.apiKey = apiKey;
        this.modelo = modelo;
        this.habilitado = habilitado;
    }

    @Override
    public ResultadoAnalisisIa analizar(List<TurnoParseado> turnosRelevantes) {
        if (!habilitado || apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Analisis IA deshabilitado o sin OPENAI_API_KEY -- ver app.omnicanal.ia-habilitada.");
        }

        String textoConversacion = turnosRelevantes.stream()
                .map(t -> "[" + t.autor().name() + "] " + t.nombre() + " (" + t.fechaTexto() + "): " + t.mensaje())
                .reduce((a, b) -> a + "\n\n" + b).orElse("");

        String contenido = llamarConReintentos(construirPrompt(textoConversacion));
        return parsearRespuesta(contenido);
    }

    private String llamarConReintentos(String prompt) {
        RuntimeException ultimoError = new IllegalStateException("OpenAI: sin intentos realizados");

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try {
                Map<String, Object> cuerpo = Map.of(
                        "model", modelo,
                        "response_format", Map.of("type", "json_object"),
                        "temperature", 0,
                        "messages", List.of(
                                Map.of("role", "system", "content", PROMPT_SISTEMA),
                                Map.of("role", "user", "content", prompt)));

                JsonNode respuesta = restClient.post()
                        .uri("/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(cuerpo)
                        .retrieve()
                        .body(JsonNode.class);

                String contenido = respuesta.path("choices").path(0).path("message").path("content").asText(null);
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
            String resultadoTexto = n.path("resultado").asText(null);
            Resultado resultado = null;
            if (resultadoTexto != null && RESULTADOS_PERMITIDOS.contains(resultadoTexto.toLowerCase())) {
                resultado = Resultado.valueOf(resultadoTexto.toUpperCase());
            }

            List<String> temas = new ArrayList<>();
            if (n.has("temas") && n.get("temas").isArray()) {
                n.get("temas").forEach(t -> temas.add(t.asText()));
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
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private Boolean boolONull(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        return (v == null || v.isNull() || !v.isBoolean()) ? null : v.asBoolean();
    }

    private static final String PROMPT_SISTEMA =
            "Eres un auditor de calidad de servicio al cliente para GuajiraNet, un proveedor de internet (ISP) "
            + "en La Guajira, Colombia. Tu criterio debe ser el de un supervisor exigente: NO das por resuelto "
            + "nada que no se haya resuelto de verdad en el texto. Distingues con precision entre un mensaje "
            + "automatico de cortesia y una respuesta real. Pero una respuesta NEGATIVA clara (ej. \"no hay "
            + "cobertura\") SI resuelve la duda del cliente. Nunca inventas datos que no esten en la conversacion. "
            + "Respondes UNICAMENTE con un objeto JSON valido, sin texto adicional ni bloques de codigo.";

    private String construirPrompt(String textoConversacion) {
        return """
                Analiza esta conversacion de atencion al cliente de GuajiraNet, proveedor de internet.

                La conversacion es un CASO CERRADO E INDEPENDIENTE. No uses ni inventes informacion de otras \
                conversaciones del mismo cliente.

                Cada turno esta marcado con quien hablo: [CLIENTE], [BOT], [ASESOR]. Los turnos ya fueron \
                filtrados: no incluyen encuestas, saludos automaticos aislados, menus ni acuses de recibo.

                =====================================================
                CONVERSACION
                =====================================================
                \"\"\"
                %s
                \"\"\"

                "resultado" mide si la CONSULTA del cliente quedo respondida en el chat (no si consiguio lo que \
                queria). Valores: "resuelto", "no_resuelto", "escalado".

                "resuelto": se entrego el dato pedido; se ejecuto y confirmo la gestion; el cliente confirmo que \
                se soluciono; la empresa dio una respuesta clara y definitiva aunque sea negativa o corta ("no \
                señor", "si, ya esta activo"); el cliente cierra agradeciendo tras atencion sustantiva; \
                confirmaciones de pago sin dato pendiente. EXCEPCION de pago: si el cliente pidio un dato \
                concreto para pagar (valor, cuenta, QR, link) y nunca se le dio, sigue "no_resuelto".

                "escalado": la empresa remitio formalmente el caso a un area concreta con accion definida fuera \
                del chat (visita tecnica, revision, reconexion). La plantilla de remision a soporte / \
                despacho de tecnico SIEMPRE es "escalado", aunque diga "segun orden de llegada".

                "no_resuelto": todo lo demas -- sin respuesta clara, solo pidio datos sin resolver, o el cliente \
                entrego datos y la empresa no continuo.

                tipo_ultimo_mensaje_empresa: ultimo mensaje relevante de la empresa -- "promesa_incumplida" \
                (prometio retomar y nadie volvio a escribir), "pidio_datos", "respuesta_real", "despedida", \
                "otro", "ninguno".

                fcr: true solo si resultado="resuelto" y no quedo nada pendiente.
                gestion_pendiente: true solo si la EMPRESA dejo algo pendiente de su lado.
                trato_inadecuado: true solo si hubo groseria o negativa injustificada explicita.
                oportunidad_venta: true si mostro interes real en contratar/ampliar/cambiar servicio.
                venta_confirmada_en_texto: true solo si el texto confirma pago/contrato/instalacion agendada; \
                false si solo hay interes; null si no aplica.
                sentimiento_inicial/final: positivo | neutral | negativo, del CLIENTE.
                esfuerzo_cliente: bajo | medio | alto.
                revisar_limite: true si mezcla 2+ asuntos distintos.

                categoria_oficina (uno o null): CONTRATOS, PLANES Y PROMOCIONES, TRASLADO, FACTURACION, RETIROS, \
                MEDIOS DE PAGO, PAGOS Y CARTERA, PQR, SUCESION, REAJUSTE DEL SERVICIO.
                motivo_contacto (uno): soporte, facturacion, reconexion, ventas, PQR, cobertura, informacion.
                area_destino: comercial | soporte | facturacion | retencion/PQR | null.
                municipio/barrio: tal como los escribio el cliente, sin corregir; si no aparecen, null.

                Responde UNICAMENTE con este JSON, sin texto ni markdown, todas las claves una vez, null/true/false \
                reales:
                {
                  "razonamiento": "3 a 5 frases: que pidio, que recibio, por que ese resultado, ultimo mensaje de \
                la empresa y por que, remision si aplica",
                  "motivo_contacto": "...", "submotivo": "etiqueta corta" | null,
                  "categoria_oficina": "..." | null, "municipio": "..." | null, "barrio": "..." | null,
                  "area_destino": "..." | null,
                  "resumen_motivo": "...", "resumen_desenlace": "...",
                  "sentimiento_inicial": "...", "sentimiento_final": "...",
                  "resultado": "resuelto" | "no_resuelto" | "escalado",
                  "tipo_ultimo_mensaje_empresa": "...",
                  "fcr": true|false, "esfuerzo_cliente": "...", "temas": ["hasta 4"],
                  "oportunidad_venta": true|false, "venta_confirmada_en_texto": true|false|null,
                  "trato_inadecuado": true|false, "gestion_pendiente": true|false, "revisar_limite": true|false
                }
                """.formatted(textoConversacion);
    }
}
