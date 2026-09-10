package com.marcablanca.platform.omnicanal.application.port.in;

/**
 * Reconstruye la atribucion "viene de ads" consultando la API de LIWA por
 * contacto (custom field). Marca la conversacion y, si vino de ads, su caso
 * mas antiguo y el analisis de ese caso. Accion puntual del tenant sobre su
 * propia data.
 */
public interface EjecutarBackfillDeAds {

    /**
     * @param contactos  contactos consultados.
     * @param conAds      contactos marcados como "viene de ads".
     * @param sinDato     contactos que LIWA no pudo responder (sin token, error, etc.).
     */
    record ResultadoBackfill(int contactos, int conAds, int sinDato) {
    }

    ResultadoBackfill ejecutar();
}
