package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.domain.PerfilDeAnalisisOmnicanal;

import java.util.List;
import java.util.Map;

/**
 * El perfil que se usa cuando una empresa no configuro el suyo en
 * omnicanal.tbl_configuracion_omnicanal. Es, palabra por palabra, el
 * vocabulario del pipeline original de GuajiraNet-ISP: mientras no haya un
 * segundo tenant, nada cambia; cuando lo haya, ese tenant define su propio
 * perfil sin tocar codigo.
 *
 * Vive en -infrastructure a proposito: el dominio define la forma
 * (PerfilDeAnalisisOmnicanal), no los datos de ninguna empresa.
 *
 * Las frases/opciones se dejan tal cual se escribieron (con tildes, "sueño"):
 * FiltroDeRelevancia las normaliza en su constructor antes de comparar. El
 * pipeline original tenia un bug aca -- comparaba "sueño" literal contra texto
 * ya sin acentos y nunca casaba; ahora si.
 */
public final class PerfilDeAnalisisPredeterminado {

    private PerfilDeAnalisisPredeterminado() {
    }

    public static final PerfilDeAnalisisOmnicanal ISP = new PerfilDeAnalisisOmnicanal(
            "GuajiraNet",
            PROMPT_SISTEMA(),
            PLANTILLA_PROMPT(),
            "https://chat.liwa.co",
            "587226",
            List.of(
                    "si", "no", "oficinas", "contratos", "promociones", "planes y promociones",
                    "soporte tecnico", "facturacion", "retiros", "pqr", "medios de pago",
                    "pagos y cartera", "reajuste del servicio", "sucesion", "traslado",
                    "san juan", "fonseca", "albania", "distraccion", "hatonuevo", "riohacha",
                    "barrancas", "maicao", "villanueva", "urumita", "el molino", "molino",
                    "dibulla", "buenavista", "la jagua", "uribia", "manaure"),
            List.of(
                    "gracias por comunicarse con nosotros",
                    "gracias por comunicarnos",
                    "por favor diligencia este enlace",
                    "diligenciar el siguiente formulario"),
            List.of(
                    "gracias por preferirnos",
                    "esperamos poder servirte nuevamente",
                    "guajiranet conectando sueño",
                    "somos guajiranet conectando",
                    "recuerde somos guajiranet"),
            List.of(
                    "Riohacha", "Albania", "Barrancas", "Dibulla", "Distracción",
                    "Fonseca", "Hatonuevo", "La Jagua del Pilar", "Maicao", "Manaure",
                    "Molino", "San Juan del Cesar", "Uribia", "Urumita", "Villanueva",
                    "Buenavista",
                    "Valledupar", "Aguachica", "Agustín Codazzi", "Astrea", "Becerril",
                    "Bosconia", "Chimichagua", "Chiriguaná", "Curumaní", "El Copey",
                    "El Paso", "Gamarra", "González", "La Gloria", "La Jagua de Ibirico",
                    "La Paz", "Manaure Balcón del Cesar", "Pailitas", "Pelaya",
                    "Pueblo Bello", "Río de Oro", "San Alberto", "San Diego",
                    "San Martín", "Tamalameque", "Guacoche"),
            Map.of(
                    "san juan", "San Juan del Cesar",
                    "sanjuan", "San Juan del Cesar",
                    "codazzi", "Agustín Codazzi",
                    "la jagua", "La Jagua de Ibirico"),
            List.of("null", "n/a", "na", "ninguno", "no aplica", "la guajira", "guajira", "cesar"));

    private static String PROMPT_SISTEMA() {
        return "Eres un auditor de calidad de servicio al cliente para GuajiraNet, un proveedor de internet (ISP) "
                + "en La Guajira, Colombia. Tu criterio debe ser el de un supervisor exigente: NO das por resuelto "
                + "nada que no se haya resuelto de verdad en el texto. Distingues con precision entre un mensaje "
                + "automatico de cortesia y una respuesta real. Pero una respuesta NEGATIVA clara (ej. \"no hay "
                + "cobertura\") SI resuelve la duda del cliente. Nunca inventas datos que no esten en la conversacion. "
                + "Respondes UNICAMENTE con un objeto JSON valido, sin texto adicional ni bloques de codigo.";
    }

    private static String PLANTILLA_PROMPT() {
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
                """;
    }
}
