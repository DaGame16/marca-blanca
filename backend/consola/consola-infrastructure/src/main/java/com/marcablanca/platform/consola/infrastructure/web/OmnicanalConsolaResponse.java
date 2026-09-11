package com.marcablanca.platform.consola.infrastructure.web;

public record OmnicanalConsolaResponse(String webhookUrl, String webhookSecret, boolean iaHabilitada,
                                       String openaiModelo, boolean liwaTokenConfigurado) {
}
