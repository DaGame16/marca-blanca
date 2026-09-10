package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.domain.FiltroDeRelevancia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden test: con el perfil ISP por defecto, el filtro tiene que clasificar
 * exactamente igual que el pipeline estatico original. Si esto cambia, cambia
 * que conversaciones se analizan para toda empresa sin perfil propio.
 */
class FiltroDeRelevanciaGoldenTest {

    private final FiltroDeRelevancia filtro = new FiltroDeRelevancia(PerfilDeAnalisisPredeterminado.ISP);

    @Test
    void ruido_conocido_se_filtra() {
        assertTrue(filtro.esTurnoNoRelevante("riohacha"));
        assertTrue(filtro.esTurnoNoRelevante("marca la opcion 2 para soporte tecnico"));
        assertTrue(filtro.esTurnoNoRelevante("hola buenas tardes"));
        assertTrue(filtro.esTurnoNoRelevante("quiero mas informacion"));
        assertTrue(filtro.esTurnoNoRelevante("un asesor se comunicara contigo en breve"));
    }

    @Test
    void frase_marca_con_tilde_en_el_perfil_igual_casa() {
        // El perfil trae "guajiranet conectando sueño" (con ñ); el filtro la
        // normaliza, asi que casa contra el texto ya sin acentos.
        assertTrue(filtro.esTurnoNoRelevante("con guajiranet conectando suenos de la region"));
        assertTrue(filtro.esTurnoNoRelevante("con guajiranet conectando sueños de la region"));
    }

    @Test
    void contenido_real_no_se_filtra() {
        assertFalse(filtro.esTurnoNoRelevante("no tengo internet hace tres dias"));
        assertFalse(filtro.esTurnoNoRelevante("no aun no me funciona el modem"));
    }

    @Test
    void encuestas_conocidas() {
        assertTrue(filtro.esTurnoDeEncuesta("https://docs.google.com/forms/d/e/abc"));
        assertTrue(filtro.esTurnoDeEncuesta("Gracias por comunicarse con nosotros"));
        assertFalse(filtro.esTurnoDeEncuesta("no tengo internet"));
    }
}
