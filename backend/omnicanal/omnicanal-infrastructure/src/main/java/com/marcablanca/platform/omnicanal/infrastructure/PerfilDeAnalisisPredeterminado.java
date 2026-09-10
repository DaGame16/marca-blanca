package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.domain.PerfilDeAnalisisOmnicanal;

import java.util.List;
import java.util.Map;

/**
 * El perfil que se usa cuando una empresa no configuro el suyo en
 * omnicanal.tbl_configuracion_omnicanal. Es, palabra por palabra, el
 * vocabulario y el prompt del pipeline original de GuajiraNet-ISP (el
 * modulo liwa-webhook del ERP en NestJS): mientras no haya un segundo
 * tenant, nada cambia; cuando lo haya, ese tenant define su propio perfil
 * sin tocar codigo.
 *
 * Vive en -infrastructure a proposito: el dominio define la forma
 * (PerfilDeAnalisisOmnicanal), no los datos de ninguna empresa.
 *
 * Las frases/opciones se dejan tal cual se escribieron (con tildes):
 * FiltroDeRelevancia normaliza sus entradas en el constructor antes de
 * comparar.
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
                + "nada que no se haya resuelto de verdad en el texto. Distingues con precisión entre un mensaje "
                + "automático de cortesía y una respuesta real. Pero una respuesta NEGATIVA clara (ej. \"no hay "
                + "cobertura\") SÍ resuelve la duda del cliente. Nunca inventas datos que no estén en la conversación. "
                + "Respondes ÚNICAMENTE con un objeto JSON válido, sin texto adicional ni bloques de código.";
    }

    /**
     * Prompt del mensaje "user" -- integro del liwa-webhook de NestJS
     * (analisis-ia.service.ts, construirPrompt). El unico %s es donde se
     * inyecta la conversacion; los \"\"\" delimitan ese bloque.
     */
    private static String PLANTILLA_PROMPT() {
        return """
                Analiza esta conversación de atención al cliente de GuajiraNet, proveedor de internet.

                La conversación es un CASO CERRADO E INDEPENDIENTE. No uses ni inventes información de otras conversaciones del mismo cliente. No supongas que una conversación quedó resuelta solo porque existe un cierre técnico, una encuesta o un mensaje automático.

                Cada turno está marcado con quién habló:
                - [CLIENTE]: persona que solicita información o atención.
                - [BOT]: respuesta automática de la empresa.
                - [ASESOR]: persona real de la empresa.

                Los turnos que te llegan YA fueron filtrados: no incluyen encuestas de satisfacción, saludos automáticos aislados, menús ni acuses de recibo tipo "en breve un asesor te atenderá". Analiza solo lo que ves.

                =====================================================
                CONVERSACIÓN
                =====================================================
                \"\"\"
                %s
                \"\"\"

                =====================================================
                1. REGLA CENTRAL DE "resultado"
                =====================================================

                "resultado" mide UNA sola cosa: ¿la CONSULTA o DUDA del cliente quedó respondida en el chat? NO mide si el cliente consiguió lo que quería.

                VALORES POSIBLES: "resuelto", "no_resuelto", "escalado". No existe "abandonado" como valor de "resultado".

                EVALÚA TODA LA CONVERSACIÓN, NO SOLO EL ÚLTIMO MENSAJE. Un mensaje final de despedida o cortesía de la empresa ("muchas gracias por preferirnos", "quedamos atentos", "feliz día") NUNCA borra una respuesta sustantiva que la empresa haya dado antes. Si en un turno anterior el asesor explicó el motivo, dio el dato o resolvió la duda, el caso es "resuelto" aunque el ÚLTIMO turno sea solo una despedida. No escribas en el resumen que "la empresa no respondió" cuando sí hubo una respuesta con contenido en cualquier punto del hilo.

                --- "resuelto" ---

                Marca "resuelto" en CUALQUIERA de estos casos:

                1. Se entregó el dato pedido (precio, plan, saldo, fecha, clave, horario, etc.).
                2. Se ejecutó la gestión y se confirmó.
                3. El cliente confirmó que su problema quedó solucionado.
                4. La empresa dio una respuesta CLARA Y DEFINITIVA, aunque sea NEGATIVA. Ejemplos que SON "resuelto":
                   - "En tu zona no tenemos cobertura." → la duda de cobertura quedó resuelta, aunque el cliente no obtenga servicio.
                   - "Ese plan de menos de 30.000 no existe."
                   - "No se puede hacer ese trámite por este canal, debe ir a la oficina."
                   Que la respuesta no le guste al cliente NO la hace "no_resuelto".
                   Una respuesta MUY CORTA del agente también cuenta como respuesta clara si contesta directamente lo que se preguntó. Si el cliente hizo una pregunta de sí/no ("¿tienen cobertura en X?", "¿se puede pagar por Nequi?", "¿ya está activo?") y el agente respondió "no señor", "no señora", "no", "sí señor", "ya está activo", "del 1 al 6 de cada mes", o similar, ESO es una respuesta real y definitiva → "resuelto". NO lo clasifiques como "no_resuelto" ni digas en el resumen que "no recibió respuesta": sí la recibió, aunque fuera de una sola línea.
                5. La conversación TERMINA con el cliente AGRADECIENDO o dando acuse ("ok, gracias", "listo", "muchas gracias", "vale, gracias") DESPUÉS de que la empresa le dio una respuesta o atención sustantiva. Si el cliente agradece, es porque se le atendió de forma provechosa → "resuelto".
                   Excepción: si después del "gracias" el cliente hace una PREGUNTA NUEVA que queda sin responder, NO es "resuelto".
                6. Confirmaciones de pago ("gracias por su pago", "pago recibido", "ya se confirmó su pago", "su pago fue registrado"): SON "resuelto" cuando el cliente solo informó, reportó o confirmó que iba a pagar / ya pagó, sin dejar pendiente ningún dato concreto — ese acuse de la empresa SÍ es una respuesta real y definitiva, no una simple cortesía.
                   Pero si el cliente pidió específicamente un dato para poder pagar (el VALOR exacto a pagar, un número de cuenta o llave, un código QR, un link de pago, un medio alternativo porque el habitual no le sirve) y la empresa nunca se lo entregó en el chat, el caso sigue "no_resuelto" aunque más adelante aparezca un "gracias por su pago": ese agradecimiento confirma que el cliente pagó por su cuenta, no que la empresa resolvió lo que pidió. En este caso SÍ debes escribir en el resumen que la empresa no entregó el dato solicitado.

                RECUERDA no ser más estricto de lo necesario: si a lo largo del chat la empresa SÍ contestó la duda del cliente y la dejó clara (aunque sea en un mensaje corto, aunque sea negativa, aunque el cierre sea solo un agradecimiento), eso cuenta como algo positivo y el resultado debe ser "resuelto". La regla de arriba sobre pagos es una excepción puntual para cuando queda un dato pedido explícitamente sin entregar — no una invitación a exigir más de lo que el cliente realmente pidió.

                --- "escalado" ---

                La empresa remitió formalmente el caso a un área o zona CONCRETA con una ACCIÓN DEFINIDA que ocurrirá fuera del chat (programar visita técnica, revisar físicamente el servicio, ejecutar una reconexión). Debe existir área identificable + acción concreta. "Lo estamos validando" sin más NO es escalado.
                Si tras la remisión el cliente cierra con "ok/gracias" sin abrir nada nuevo, se mantiene "escalado".

                REGLA DURA — la plantilla de remisión a soporte SIEMPRE es "escalado":
                Si en la conversación aparece un mensaje de la empresa que dice, en cualquier variante, que "el caso fue remitido / enviado al área de soporte (o al área encargada, o a la zona)", que "será ingresado en la programación", que "un técnico se desplazará / visitará la residencia para validar y reparar", o que "ya su servicio está en el área encargada" — eso ES una remisión formal con acción definida (despacho de un técnico). El resultado es "escalado", aunque el mensaje también diga que "los técnicos trabajan según el orden de llegada / bajo programación" y aunque no den fecha exacta. Esa coletilla de "orden de llegada" NO lo degrada a "no_resuelto".
                La única excepción: si DESPUÉS de esa remisión el cliente vuelve y reporta que el problema sigue sin resolverse y la empresa no responde nada nuevo, entonces sí es "no_resuelto".

                --- "no_resuelto" ---

                Todo lo demás: la consulta del cliente quedó sin una respuesta clara. Por ejemplo:
                - El cliente preguntó algo y nadie respondió con contenido.
                - La empresa solo pidió datos / prometió atención y no entregó nada.
                - La empresa respondió otra cosa distinta a lo que se preguntó.
                - El cliente entregó datos y la empresa no continuó.
                - El cliente nunca formuló una petición concreta y la empresa tampoco resolvió nada.

                =====================================================
                2. tipo_ultimo_mensaje_empresa
                =====================================================

                Identifica el ÚLTIMO mensaje RELEVANTE escrito por el lado de la empresa (BOT o ASESOR) y clasifícalo. El código lo usa para saber quién cortó el hilo.

                - "promesa_incumplida": el último mensaje de la empresa prometía retomar el contacto, validar y avisar, o que un asesor escribiría — y NADIE del lado de la empresa volvió a escribir. Ejemplos: "un asesor se comunicará contigo", "quedamos pendientes de validar y te confirmamos", "ya paso el caso y te contactan".
                - "pidio_datos": el último mensaje de la empresa pedía datos, una confirmación, una foto o una acción concreta que el cliente debía hacer para continuar, y el cliente no lo hizo.
                - "respuesta_real": el último mensaje de la empresa entregó una respuesta sustantiva (positiva o negativa) a lo que el cliente pedía.
                - "despedida": el último mensaje de la empresa fue un cierre/despedida tras haber atendido el asunto.
                - "otro": el último mensaje de la empresa no encaja en lo anterior.
                - "ninguno": la empresa NUNCA escribió un mensaje relevante en la conversación.

                OJO — no confundas el mensaje automático inicial con el último mensaje real: el bot suele responder de entrada "en breve un asesor te atenderá" (eso SÍ es una promesa), pero si DESPUÉS un asesor humano efectivamente escribió y esa promesa se cumplió, ese mensaje automático deja de ser el relevante. Busca el ÚLTIMO mensaje de la empresa en el tiempo, no el primero que "suene" a promesa. Si ese último mensaje es el asesor pidiendo datos concretos (dirección, cédula, foto, confirmación) para poder continuar, es "pidio_datos", NUNCA "promesa_incumplida" — aunque antes haya aparecido el mensaje automático de bienvenida.

                =====================================================
                3. OTROS CAMPOS
                =====================================================

                fcr: true solo si el cliente obtuvo dentro de ESTA conversación lo que necesitaba y no quedó nada pendiente. Regla dura: si "resultado" no es "resuelto", fcr = false. Una respuesta negativa clara y definitiva puede tener fcr = true.

                gestion_pendiente: true solo si la EMPRESA dejó algo pendiente de su lado (prometió retomar y no lo hizo; el cliente entregó lo pedido y la empresa no continuó; inició una gestión y no la confirmó). Si la empresa ya hizo lo que le tocaba y lo pendiente depende del cliente, gestion_pendiente = false.

                trato_inadecuado: true solo si hubo grosería, sarcasmo, descortesía explícita o negativa injustificada a atender ("no voy a revisar eso", "ya le dije, no insista"). Responder tarde, no responder o pedir datos NO es trato inadecuado.

                oportunidad_venta: true si el cliente mostró interés real en contratar, ampliar, cambiar o cotizar un servicio (precios, planes, promociones, cobertura para instalar, condiciones de contratación). false si el contacto fue solo por soporte, factura, pago, retiro, PQR o reconexión.

                venta_confirmada_en_texto: true solo si el texto confirma pago realizado, contrato firmado, instalación agendada o cierre comercial inequívoco; false si solo hay interés o cotización; null si no hay contexto comercial.

                sentimiento_inicial / sentimiento_final: positivo | neutral | negativo. Evalúa el tono del CLIENTE, no el del bot. El final refleja el último mensaje relevante del cliente.

                esfuerzo_cliente: bajo (consulta sencilla) | medio (entrega de datos, varias preguntas, seguimiento) | alto (múltiples intentos, repetición de datos, pruebas técnicas, pagos, documentos, espera prolongada).

                revisar_limite: true si la conversación mezcla dos o más asuntos suficientemente distintos como para requerir casos separados.

                =====================================================
                4. CATÁLOGOS
                =====================================================

                categoria_oficina — exactamente uno o null:
                CONTRATOS, PLANES Y PROMOCIONES, TRASLADO, FACTURACIÓN, RETIROS, MEDIOS DE PAGO, PAGOS Y CARTERA, PQR, SUCESIÓN, REAJUSTE DEL SERVICIO.

                motivo_contacto — exactamente uno:
                soporte, facturación, reconexión, ventas, PQR, cobertura, información.

                submotivo: etiqueta corta de 2 a 4 palabras o null ("cambio de clave", "internet sin servicio", "precio triple combo", "pago no reflejado").

                municipio: el municipio/ciudad tal como lo escribió el cliente, sin corregir. "La Guajira" o "Cesar" solos → null. Si no aparece → null.

                barrio: el barrio/sector tal como lo escribió el cliente, sin corregir. Si no aparece → null.

                area_destino: comercial | soporte | facturación | retención/PQR | null. Quién debía atender principalmente el caso.

                =====================================================
                5. FORMATO DE SALIDA OBLIGATORIO
                =====================================================

                Responde ÚNICAMENTE con un objeto JSON válido. Sin Markdown, sin texto antes ni después. Todas las claves exactamente una vez. Usa null real y true/false reales, no cadenas.

                El campo "razonamiento" debe tener entre 3 y 5 frases y explicar, en orden:
                1. qué pidió el cliente (o que no formuló una petición);
                2. qué recibió realmente, distinguiendo cortesía de respuesta;
                3. por qué elegiste ese "resultado" (y si aplica, por qué una respuesta negativa cuenta como "resuelto", o por qué un "gracias" final cuenta como "resuelto");
                4. cuál fue el último mensaje relevante de la empresa y por qué le pusiste ese "tipo_ultimo_mensaje_empresa";
                5. si hubo remisión formal, cuál fue el área y la acción definida.

                Estructura obligatoria:
                {
                  "razonamiento": "...",
                  "motivo_contacto": "soporte" | "facturación" | "reconexión" | "ventas" | "PQR" | "cobertura" | "información",
                  "submotivo": "etiqueta corta" | null,
                  "categoria_oficina": "CONTRATOS" | "PLANES Y PROMOCIONES" | "TRASLADO" | "FACTURACIÓN" | "RETIROS" | "MEDIOS DE PAGO" | "PAGOS Y CARTERA" | "PQR" | "SUCESIÓN" | "REAJUSTE DEL SERVICIO" | null,
                  "municipio": "texto exacto" | null,
                  "barrio": "texto exacto" | null,
                  "area_destino": "comercial" | "soporte" | "facturación" | "retención/PQR" | null,
                  "resumen_motivo": "1 o 2 frases sobre lo que necesitaba el cliente",
                  "resumen_desenlace": "1 o 2 frases sobre lo que recibió y cómo terminó",
                  "sentimiento_inicial": "positivo" | "neutral" | "negativo",
                  "sentimiento_final": "positivo" | "neutral" | "negativo",
                  "resultado": "resuelto" | "no_resuelto" | "escalado",
                  "tipo_ultimo_mensaje_empresa": "promesa_incumplida" | "pidio_datos" | "respuesta_real" | "despedida" | "otro" | "ninguno",
                  "fcr": true | false,
                  "esfuerzo_cliente": "bajo" | "medio" | "alto",
                  "temas": ["máximo 4 etiquetas cortas"],
                  "oportunidad_venta": true | false,
                  "venta_confirmada_en_texto": true | false | null,
                  "trato_inadecuado": true | false,
                  "gestion_pendiente": true | false,
                  "revisar_limite": true | false
                }

                VALIDACIÓN FINAL:
                - Una respuesta NEGATIVA pero clara y definitiva es "resuelto", no "no_resuelto". Vale igual si es de una sola línea ("no señor", "no señora", "sí, ya está activo").
                - Un cierre del cliente con "gracias" tras atención sustantiva es "resuelto".
                - Si la empresa dijo que remitió el caso al área de soporte / área encargada / zona, y que un técnico se desplazará a validar y reparar, el resultado es "escalado" (aunque añada "según el orden de llegada").
                - Si en el resumen vas a escribir "no recibió respuesta" o "no hubo respuesta de la empresa", relee la conversación: solo es válido si de verdad NO hay ningún turno [ASESOR] ni [BOT] con contenido. Un "no señor@" o un "listo" del agente SÍ son respuesta.
                - Si "resultado" no es "resuelto", "fcr" debe ser false.
                - "tipo_ultimo_mensaje_empresa" describe SIEMPRE el último mensaje de la empresa, aunque "resultado" sea "resuelto".
                - Un "gracias por su pago" es "resuelto" salvo que el cliente haya pedido explícitamente un dato de pago (monto, cuenta, llave, QR, link, medio alternativo) que nunca se entregó — en ese caso puntual sigue "no_resuelto". Fuera de esa excepción, no seas más estricto de lo necesario: si la empresa sí despejó la duda del cliente en algún punto del chat, eso es "resuelto".
                """.strip();
    }
}
