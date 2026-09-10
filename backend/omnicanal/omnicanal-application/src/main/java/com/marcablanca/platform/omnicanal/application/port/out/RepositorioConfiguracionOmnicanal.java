package com.marcablanca.platform.omnicanal.application.port.out;

import com.marcablanca.platform.omnicanal.domain.PerfilDeAnalisisOmnicanal;

/**
 * Configuracion de omnicanal de la empresa activa en la peticion
 * (ContextoEmpresaActual). Vive en la base de esa empresa
 * (omnicanal.tbl_configuracion_omnicanal); si no hay fila, el adaptador
 * responde con los valores por defecto (perfil ISP, IA apagada, sin token).
 */
public interface RepositorioConfiguracionOmnicanal {

    ConfiguracionDeTenant deLaEmpresaActiva();

    /** Ajustes editables por el tenant (no incluye el token). Sobrescribe la fila (upsert). */
    void guardarAjustes(boolean iaHabilitada, String openaiModelo, String liwaBaseUrl, String liwaCustomFieldAds);

    /** Define el token de LIWA (se guarda cifrado). null o vacio => lo borra. */
    void guardarLiwaToken(String tokenPlano);

    /**
     * @param perfil        prompt de IA + datos de LIWA (nunca null).
     * @param liwaApiToken   token de la cuenta LIWA, ya descifrado; null si no hay.
     * @param iaHabilitada   si false, AnalizadorDeConversacion no debe llamar al motor.
     * @param openaiModelo   modelo a usar; null => el default de la plataforma.
     */
    record ConfiguracionDeTenant(PerfilDeAnalisisOmnicanal perfil, String liwaApiToken,
                                 boolean iaHabilitada, String openaiModelo) {
    }
}
