package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.ConfigurarOmnicanal.VistaConfig;
import com.marcablanca.platform.omnicanal.application.port.out.RegistroRuteoOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal.ConfiguracionDeTenant;
import com.marcablanca.platform.omnicanal.domain.PerfilDeAnalisisOmnicanal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurarOmnicanalServiceTest {

    @Mock
    RepositorioConfiguracionOmnicanal configuracion;
    @Mock
    RegistroRuteoOmnicanal ruteo;

    private static final PerfilDeAnalisisOmnicanal PERFIL = new PerfilDeAnalisisOmnicanal(
            "Empresa", "s", "u %s", "https://chat.liwa.co", "587226",
            List.of(), List.of(), List.of(), List.of(), Map.of(), List.of());

    private ConfigurarOmnicanalService servicio() {
        return new ConfigurarOmnicanalService(configuracion, ruteo, "https://api.miportal.com");
    }

    @Test
    void ver_arma_la_url_del_webhook_y_no_expone_el_token() {
        when(ruteo.secretoWebhook()).thenReturn("secreto-123");
        when(configuracion.deLaEmpresaActiva())
                .thenReturn(new ConfiguracionDeTenant(PERFIL, "token-real", true, "gpt-4o"));

        VistaConfig v = servicio().ver();

        assertEquals("https://api.miportal.com/api/v1/omnicanal/webhook/chat-history", v.webhookUrl());
        assertEquals("secreto-123", v.webhookSecret());
        assertTrue(v.iaHabilitada());
        assertEquals("gpt-4o", v.openaiModelo());
        assertEquals("https://chat.liwa.co", v.liwaBaseUrl());
        assertEquals("587226", v.liwaCustomFieldAds());
        assertTrue(v.liwaTokenConfigurado());
    }

    @Test
    void ver_sin_token_marca_no_configurado() {
        when(ruteo.secretoWebhook()).thenReturn("s");
        when(configuracion.deLaEmpresaActiva())
                .thenReturn(new ConfiguracionDeTenant(PERFIL, null, false, null));

        assertFalse(servicio().ver().liwaTokenConfigurado());
    }

    @Test
    void rotar_secreto_usa_el_puerto_de_ruteo() {
        when(ruteo.rotarSecretoWebhook()).thenReturn("secreto-nuevo");
        lenient().when(configuracion.deLaEmpresaActiva())
                .thenReturn(new ConfiguracionDeTenant(PERFIL, null, false, null));

        assertEquals("secreto-nuevo", servicio().rotarSecreto().webhookSecret());
    }

    @Test
    void actualizar_ajustes_normaliza_vacios_a_null() {
        servicio().actualizarAjustes(true, "  ", "https://x  ", "");

        verify(configuracion).guardarAjustes(true, null, "https://x", null);
    }

    @Test
    void definir_token_vacio_equivale_a_borrarlo() {
        servicio().definirLiwaToken("   ");
        verify(configuracion).guardarLiwaToken(null);
    }

    @Test
    void borrar_token() {
        servicio().borrarLiwaToken();
        verify(configuracion).guardarLiwaToken(null);
    }
}
