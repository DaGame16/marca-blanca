package com.marcablanca.platform.omnicanal.application.port.in;

/**
 * Self-service del tenant sobre su omnicanal: ver la URL/secreto del webhook
 * para pegarlos en LIWA, cargar el token de LIWA y prender el analisis IA.
 * La empresa sale del JWT (ContextoEmpresaActual); este puerto no la recibe.
 * El gate de modulo activo ya lo aplica InterceptorModuloOmnicanal.
 */
public interface ConfigurarOmnicanal {

    /**
     * @param webhookUrl           URL completa que se pega en LIWA.
     * @param webhookSecret        secreto del header x-liwa-webhook-secret (se crea si no existia).
     * @param iaHabilitada         si el analisis IA esta prendido para esta empresa.
     * @param openaiModelo         modelo configurado; null => el default de plataforma.
     * @param liwaBaseUrl          base de la API de LIWA.
     * @param liwaCustomFieldAds   id del custom field de atribucion de ads.
     * @param liwaTokenConfigurado si hay un token de LIWA cargado (nunca se devuelve el valor).
     */
    record VistaConfig(String webhookUrl, String webhookSecret, boolean iaHabilitada, String openaiModelo,
                       String liwaBaseUrl, String liwaCustomFieldAds, boolean liwaTokenConfigurado) {
    }

    VistaConfig ver();

    void actualizarAjustes(boolean iaHabilitada, String openaiModelo, String liwaBaseUrl, String liwaCustomFieldAds);

    void definirLiwaToken(String tokenPlano);

    void borrarLiwaToken();

    /** Genera un secreto nuevo y devuelve la vista actualizada. */
    VistaConfig rotarSecreto();
}
