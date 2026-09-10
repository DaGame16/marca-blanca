package com.marcablanca.platform.omnicanal.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba que el filtro use el vocabulario del perfil y no valores fijos:
 * opciones de menu, marcadores de encuesta, frases-marca y nombre de empresa
 * salen del PerfilDeAnalisisOmnicanal. Las reglas genericas (menu numerado,
 * acuse de recibo, "quiero informacion") no dependen del perfil.
 */
class FiltroDeRelevanciaTest {

    private static PerfilDeAnalisisOmnicanal perfil(List<String> opcionesMenu, List<String> marcadoresEncuesta,
                                                   List<String> frasesMarcaRuido, String nombreEmpresa) {
        return new PerfilDeAnalisisOmnicanal(nombreEmpresa, "system", "user %s", "https://x", "1",
                opcionesMenu, marcadoresEncuesta, frasesMarcaRuido,
                List.of(), Map.of(), List.of());
    }

    private final FiltroDeRelevancia filtro = new FiltroDeRelevancia(perfil(
            List.of("wakanda"),
            List.of("responde la encuesta acme"),
            List.of("saludos de acme corp"),
            "AcmeCorp"));

    @Test
    void una_opcion_de_menu_del_perfil_es_ruido() {
        assertTrue(filtro.esTurnoNoRelevante("Wakanda"));
    }

    @Test
    void un_lugar_de_otro_perfil_ya_no_se_filtra() {
        // "riohacha" era una opcion de menu fija en el codigo viejo; con este
        // perfil no lo es.
        assertFalse(filtro.esTurnoNoRelevante("riohacha"));
    }

    @Test
    void marcador_de_encuesta_del_perfil() {
        assertTrue(filtro.esTurnoDeEncuesta("Por favor responde la encuesta Acme cuando puedas"));
    }

    @Test
    void frase_marca_del_perfil_es_ruido() {
        assertTrue(filtro.esTurnoNoRelevante("saludos de acme corp"));
    }

    @Test
    void el_nombre_de_empresa_cuenta_como_saludo() {
        assertTrue(filtro.esTurnoNoRelevante("AcmeCorp"));
    }

    @Test
    void contenido_real_pasa() {
        assertFalse(filtro.esTurnoNoRelevante("no tengo internet hace tres dias"));
    }

    @Test
    void reglas_genericas_no_dependen_del_perfil() {
        assertTrue(filtro.esTurnoNoRelevante("marca la opcion 2 para soporte"));
        assertTrue(filtro.esTurnoNoRelevante("quiero mas informacion"));
        assertTrue(filtro.esTurnoNoRelevante("un asesor se comunicara contigo en breve"));
    }
}
