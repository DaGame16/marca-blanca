package com.marcablanca.platform.omnicanal.domain;

import java.util.List;
import java.util.Map;

/**
 * Configuracion de negocio que cambia por empresa: como se le habla al motor
 * de IA (prompt), a que cuenta de LIWA se consulta la atribucion de ads, y el
 * vocabulario que usan FiltroDeRelevancia y NormalizadorDeMunicipio para
 * separar contenido real de ruido.
 *
 * Las conversaciones de una empresa distinta de la que armo el pipeline
 * original (GuajiraNet-ISP) traen otro vocabulario -- otro nombre, otro rubro,
 * otros municipios, otras opciones de menu. Este perfil saca todo eso del
 * codigo y lo vuelve un dato por tenant. El perfil por defecto (ISP) vive en
 * -infrastructure (PerfilDeAnalisisPredeterminado): el dominio solo define la
 * forma.
 *
 * @param nombreEmpresa       nombre comercial; tambien cuenta como "saludo" en el filtro.
 * @param promptSistema       mensaje "system" completo para el motor de IA.
 * @param plantillaPrompt     plantilla del mensaje "user"; un unico %s para la conversacion.
 * @param liwaBaseUrl         base de la API de LIWA (chat.liwa.co u otra).
 * @param liwaCustomFieldAds  id del custom field de LIWA que marca "viene de ads".
 * @param opcionesMenu        respuestas de menu (ya normalizadas) que no son contenido real.
 * @param marcadoresEncuesta  substrings (ya normalizados) que delatan un mensaje de encuesta.
 * @param frasesMarcaRuido    substrings (ya normalizados) de cierre/firma de marca, sin valor de atencion.
 * @param lugaresConocidos    municipios/zonas de cobertura, tal como se quieren normalizados.
 * @param abreviaturasLugar   alias (clave normalizada) -> nombre canonico de lugar.
 * @param lugaresVacios       valores (ya normalizados) que significan "sin municipio".
 */
public record PerfilDeAnalisisOmnicanal(
        String nombreEmpresa,
        String promptSistema,
        String plantillaPrompt,
        String liwaBaseUrl,
        String liwaCustomFieldAds,
        List<String> opcionesMenu,
        List<String> marcadoresEncuesta,
        List<String> frasesMarcaRuido,
        List<String> lugaresConocidos,
        Map<String, String> abreviaturasLugar,
        List<String> lugaresVacios) {

    public PerfilDeAnalisisOmnicanal {
        opcionesMenu = List.copyOf(opcionesMenu);
        marcadoresEncuesta = List.copyOf(marcadoresEncuesta);
        frasesMarcaRuido = List.copyOf(frasesMarcaRuido);
        lugaresConocidos = List.copyOf(lugaresConocidos);
        abreviaturasLugar = Map.copyOf(abreviaturasLugar);
        lugaresVacios = List.copyOf(lugaresVacios);
    }

    /** Arma el mensaje "user" para una conversacion ya formateada como texto. */
    public String prompt(String textoConversacion) {
        return plantillaPrompt.formatted(textoConversacion);
    }
}
