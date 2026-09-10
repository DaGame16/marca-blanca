package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.domain.PerfilDeAnalisisOmnicanal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden test: el perfil ISP por defecto tiene que reproducir el prompt del
 * pipeline original (liwa-webhook del ERP en NestJS). Si algo de esto se
 * rompe, cambia el análisis de TODAS las empresas que no configuraron su
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
    void prompt_de_sistema_con_tildes_y_criterio_del_pipeline() {
        String s = isp.promptSistema();
        assertTrue(s.startsWith("Eres un auditor de calidad de servicio al cliente para GuajiraNet, "
                + "un proveedor de internet (ISP) en La Guajira, Colombia."));
        assertTrue(s.contains("Distingues con precisión entre un mensaje automático de cortesía"));
        assertTrue(s.contains("Respondes ÚNICAMENTE con un objeto JSON válido"));
    }

    @Test
    void plantilla_conserva_las_reglas_largas_del_prompt_original() {
        String p = isp.plantillaPrompt();
        assertTrue(p.contains("Analiza esta conversación de atención al cliente de GuajiraNet, proveedor de internet."));
        assertTrue(p.contains("EVALÚA TODA LA CONVERSACIÓN, NO SOLO EL ÚLTIMO MENSAJE."));
        assertTrue(p.contains("REGLA DURA — la plantilla de remisión a soporte SIEMPRE es \"escalado\""));
        assertTrue(p.contains("OJO — no confundas el mensaje automático inicial con el último mensaje real"));
        assertTrue(p.contains("VALIDACIÓN FINAL:"));
        assertTrue(p.contains("categoria_oficina — exactamente uno o null:"));
        assertTrue(p.contains("motivo_contacto — exactamente uno:"));
        assertTrue(p.contains("\"revisar_limite\": true | false"));
        assertTrue(p.contains("%s"), "la plantilla debe tener el marcador de la conversación");
    }

    @Test
    void prompt_inyecta_la_conversacion_y_no_deja_el_marcador() {
        String armado = isp.prompt("[CLIENTE] Juan (hoy): no tengo internet");
        assertTrue(armado.contains("[CLIENTE] Juan (hoy): no tengo internet"));
        assertFalse(armado.contains("%s"));
        // el bloque de la conversación queda entre los delimitadores de tres comillas
        assertTrue(armado.contains("\"\"\"\n[CLIENTE] Juan (hoy): no tengo internet\n\"\"\""));
    }

    @Test
    void plantilla_esta_recortada_como_el_trim_del_original() {
        String p = isp.plantillaPrompt();
        assertFalse(p.startsWith("\n") || p.startsWith(" "));
        assertFalse(p.endsWith("\n") || p.endsWith(" "));
    }
}
