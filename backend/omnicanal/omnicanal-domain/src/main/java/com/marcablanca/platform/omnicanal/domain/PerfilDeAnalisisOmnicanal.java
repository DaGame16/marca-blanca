package com.marcablanca.platform.omnicanal.domain;

/**
 * Configuracion de negocio que cambia por empresa: como se le habla al motor
 * de IA (prompt de sistema y plantilla del prompt de analisis) y a que cuenta
 * de LIWA se consulta la atribucion de ads.
 *
 * Las conversaciones de una empresa distinta de la que armo el pipeline
 * original (GuajiraNet-ISP) traen otro vocabulario de negocio -- otro nombre,
 * otro rubro, otra taxonomia de motivos. Este perfil saca todo eso del codigo
 * y lo vuelve un dato por tenant. El perfil por defecto (ISP) vive en
 * -infrastructure (PerfilDeAnalisisPredeterminado), no aca: el dominio solo
 * define la forma.
 *
 * En una fase siguiente este perfil suma el vocabulario que hoy sigue estatico
 * en FiltroDeRelevancia y NormalizadorDeMunicipio (lugares, opciones de menu,
 * frases-marca de ruido).
 *
 * @param nombreEmpresa       nombre comercial, para textos y trazas.
 * @param promptSistema       mensaje "system" completo para el motor de IA.
 * @param plantillaPrompt     plantilla del mensaje "user"; contiene un unico
 *                             marcador %s donde se inyecta la conversacion.
 * @param liwaBaseUrl         base de la API de LIWA (chat.liwa.co u otra).
 * @param liwaCustomFieldAds  id del custom field de LIWA que marca "viene de ads".
 */
public record PerfilDeAnalisisOmnicanal(
        String nombreEmpresa,
        String promptSistema,
        String plantillaPrompt,
        String liwaBaseUrl,
        String liwaCustomFieldAds) {

    /** Arma el mensaje "user" para una conversacion ya formateada como texto. */
    public String prompt(String textoConversacion) {
        return plantillaPrompt.formatted(textoConversacion);
    }
}
