package com.marcablanca.platform.consola.application;

/**
 * Config de omnicanal de una empresa, vista desde la consola de operacion --
 * el super admin la usa para prender/apagar la IA y rotar el secreto del
 * webhook de esa empresa. webhookSecret solo llega con valor cuando se acaba
 * de rotar (idem al self-service del tenant, ver ConfigurarOmnicanal).
 */
public record VistaOmnicanalConsola(String webhookUrl, String webhookSecret, boolean iaHabilitada,
                                    String openaiModelo, boolean liwaTokenConfigurado) {
}
