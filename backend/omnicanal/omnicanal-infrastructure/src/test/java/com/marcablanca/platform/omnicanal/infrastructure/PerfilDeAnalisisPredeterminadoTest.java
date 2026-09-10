package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.domain.PerfilDeAnalisisOmnicanal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden test: el perfil ISP por defecto tiene que reproducir, palabra por
 * palabra, el prompt que el pipeline original mandaba a OpenAI. Si algo de esto
 * se rompe, cambia el analisis de TODAS las empresas que no configuraron su
 * propio perfil.
 */
class PerfilDeAnalisisPredeterminadoTest {

    private final PerfilDeAnalisisOmnicanal isp = PerfilDeAnalisisPredeterminado.ISP;

    @Test
    void datos_de_liwa_por_defecto() {
        assertEquals("GuajiraNet", isp.nombreEmpresa());
        assertEquals("https://chat.liwa.co", isp.liwaBaseUrl());
        assertEquals("587226", isp.liwaCustomFieldAds());
    }

    @Test
    void prompt_de_sistema_conserva_el_criterio_del_pipeline_original() {
        String s = isp.promptSistema();
        assertTrue(s.startsWith("Eres un auditor de calidad de servicio al cliente para GuajiraNet, "
                + "un proveedor de internet (ISP) en La Guajira, Colombia."));
        assertTrue(s.contains("NO das por resuelto nada que no se haya resuelto de verdad"));
        assertTrue(s.contains("Respondes UNICAMENTE con un objeto JSON valido"));
    }

    @Test
    void plantilla_de_prompt_conserva_taxonomia_y_formato_de_salida() {
        String p = isp.plantillaPrompt();
        assertTrue(p.contains("Analiza esta conversacion de atencion al cliente de GuajiraNet, proveedor de internet."));
        // Los continuadores de linea "\" del text block no dejan saltos partidos.
        assertTrue(p.contains("Valores: \"resuelto\", \"no_resuelto\", \"escalado\"."));
        assertTrue(p.contains("categoria_oficina (uno o null): CONTRATOS, PLANES Y PROMOCIONES, TRASLADO, "
                + "FACTURACION, RETIROS, MEDIOS DE PAGO, PAGOS Y CARTERA, PQR, SUCESION, REAJUSTE DEL SERVICIO."));
        assertTrue(p.contains("motivo_contacto (uno): soporte, facturacion, reconexion, ventas, PQR, "
                + "cobertura, informacion."));
        assertTrue(p.contains("\"revisar_limite\": true|false"));
        assertTrue(p.contains("%s"), "la plantilla debe tener el marcador de la conversacion");
    }

    @Test
    void prompt_inyecta_la_conversacion_y_no_deja_el_marcador() {
        String armado = isp.prompt("[CLIENTE] Juan (hoy): no tengo internet");
        assertTrue(armado.contains("[CLIENTE] Juan (hoy): no tengo internet"));
        assertFalse(armado.contains("%s"));
    }
}
