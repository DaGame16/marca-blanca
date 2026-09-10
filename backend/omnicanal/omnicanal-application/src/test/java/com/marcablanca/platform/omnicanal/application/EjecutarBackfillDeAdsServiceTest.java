package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.EjecutarBackfillDeAds.ResultadoBackfill;
import com.marcablanca.platform.omnicanal.application.port.out.ClienteLiwa;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.RefContacto;
import com.marcablanca.platform.omnicanal.domain.Caso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EjecutarBackfillDeAdsServiceTest {

    @Mock
    RepositorioConversaciones repositorioConversaciones;
    @Mock
    RepositorioCasos repositorioCasos;
    @Mock
    RepositorioAnalisis repositorioAnalisis;
    @Mock
    ClienteLiwa clienteLiwa;

    private EjecutarBackfillDeAdsService servicio() {
        return new EjecutarBackfillDeAdsService(repositorioConversaciones, repositorioCasos,
                repositorioAnalisis, clienteLiwa);
    }

    private static Caso caso(long id, OffsetDateTime archivada) {
        return new Caso(id, UUID.randomUUID(), 1L, 0, 2, true, false, archivada);
    }

    @Test
    void contacto_de_ads_marca_conversacion_caso_mas_antiguo_y_su_analisis() {
        when(repositorioConversaciones.contactos()).thenReturn(List.of(new RefContacto(100L, "573001")));
        when(clienteLiwa.consultarSiVieneDeAds("573001")).thenReturn(Optional.of(true));
        // listarPorConversacion viene del mas reciente al mas viejo
        Caso reciente = caso(2L, OffsetDateTime.now());
        Caso antiguo = caso(1L, OffsetDateTime.now().minusDays(5));
        when(repositorioCasos.listarPorConversacion(100L)).thenReturn(List.of(reciente, antiguo));

        ResultadoBackfill r = servicio().ejecutar();

        verify(repositorioConversaciones).marcarVieneDeAds(100L, true);
        verify(repositorioCasos).marcarEsDeAds(1L, true);
        verify(repositorioAnalisis).marcarEsDeAdsPorCaso(1L, true);
        verify(repositorioCasos, never()).marcarEsDeAds(2L, true);
        assertEquals(new ResultadoBackfill(1, 1, 0), r);
    }

    @Test
    void contacto_sin_ads_solo_marca_la_conversacion_en_false() {
        when(repositorioConversaciones.contactos()).thenReturn(List.of(new RefContacto(100L, "x")));
        when(clienteLiwa.consultarSiVieneDeAds("x")).thenReturn(Optional.of(false));

        ResultadoBackfill r = servicio().ejecutar();

        verify(repositorioConversaciones).marcarVieneDeAds(100L, false);
        verify(repositorioCasos, never()).listarPorConversacion(100L);
        assertEquals(new ResultadoBackfill(1, 0, 0), r);
    }

    @Test
    void contacto_sin_dato_de_liwa_se_cuenta_y_no_toca_nada() {
        when(repositorioConversaciones.contactos()).thenReturn(List.of(new RefContacto(100L, "x")));
        when(clienteLiwa.consultarSiVieneDeAds("x")).thenReturn(Optional.empty());

        ResultadoBackfill r = servicio().ejecutar();

        verify(repositorioConversaciones, never()).marcarVieneDeAds(anyLong(), anyBoolean());
        assertEquals(new ResultadoBackfill(1, 0, 1), r);
    }
}
