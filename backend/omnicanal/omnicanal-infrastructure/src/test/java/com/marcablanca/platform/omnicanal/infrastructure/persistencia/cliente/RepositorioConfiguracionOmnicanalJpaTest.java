package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal.ConfiguracionDeTenant;
import com.marcablanca.platform.omnicanal.infrastructure.CifradorOmnicanal;
import com.marcablanca.platform.omnicanal.infrastructure.PerfilDeAnalisisPredeterminado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositorioConfiguracionOmnicanalJpaTest {

    @Mock
    ConfiguracionOmnicanalJpaRepository repo;

    private final CifradorOmnicanal cifrador = new CifradorOmnicanal("clave-maestra-de-prueba");
    private final ObjectMapper mapper = new ObjectMapper();

    private RepositorioConfiguracionOmnicanalJpa adaptador() {
        return new RepositorioConfiguracionOmnicanalJpa(repo, cifrador, mapper);
    }

    @Test
    void sin_fila_devuelve_todo_por_defecto() {
        when(repo.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        ConfiguracionDeTenant cfg = adaptador().deLaEmpresaActiva();

        assertSame(PerfilDeAnalisisPredeterminado.ISP, cfg.perfil());
        assertNull(cfg.liwaApiToken());
        assertFalse(cfg.iaHabilitada());
        assertNull(cfg.openaiModelo());
    }

    @Test
    void fila_sin_perfil_usa_el_perfil_por_defecto_y_descifra_el_token() {
        ConfiguracionOmnicanalEntity e = org.mockito.Mockito.mock(ConfiguracionOmnicanalEntity.class);
        when(e.getPerfilAnalisis()).thenReturn(null);
        when(e.getLiwaApiToken()).thenReturn(cifrador.cifrar("token-liwa-real"));
        when(e.isIaHabilitada()).thenReturn(true);
        lenient().when(e.getOpenaiModelo()).thenReturn("gpt-4o");
        when(repo.findFirstByOrderByIdAsc()).thenReturn(Optional.of(e));

        ConfiguracionDeTenant cfg = adaptador().deLaEmpresaActiva();

        assertSame(PerfilDeAnalisisPredeterminado.ISP, cfg.perfil());
        assertEquals("token-liwa-real", cfg.liwaApiToken());
        assertEquals(true, cfg.iaHabilitada());
        assertEquals("gpt-4o", cfg.openaiModelo());
    }

    @Test
    void perfil_json_es_override_parcial_sobre_el_por_defecto() {
        ConfiguracionOmnicanalEntity e = org.mockito.Mockito.mock(ConfiguracionOmnicanalEntity.class);
        when(e.getPerfilAnalisis()).thenReturn(
                "{\"nombreEmpresa\":\"Acme ISP\",\"liwaBaseUrl\":\"https://acme.example\"}");
        when(e.getLiwaApiToken()).thenReturn(null);
        when(repo.findFirstByOrderByIdAsc()).thenReturn(Optional.of(e));

        var perfil = adaptador().deLaEmpresaActiva().perfil();

        assertEquals("Acme ISP", perfil.nombreEmpresa());
        assertEquals("https://acme.example", perfil.liwaBaseUrl());
        // lo no sobreescrito cae al ISP
        assertEquals(PerfilDeAnalisisPredeterminado.ISP.liwaCustomFieldAds(), perfil.liwaCustomFieldAds());
        assertEquals(PerfilDeAnalisisPredeterminado.ISP.promptSistema(), perfil.promptSistema());
    }

    @Test
    void perfil_json_invalido_cae_al_perfil_por_defecto() {
        ConfiguracionOmnicanalEntity e = org.mockito.Mockito.mock(ConfiguracionOmnicanalEntity.class);
        when(e.getPerfilAnalisis()).thenReturn("{ esto no es json");
        when(e.getLiwaApiToken()).thenReturn(null);
        when(repo.findFirstByOrderByIdAsc()).thenReturn(Optional.of(e));

        assertSame(PerfilDeAnalisisPredeterminado.ISP, adaptador().deLaEmpresaActiva().perfil());
    }
}
