package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.ConsultarReportesOmnicanal.EstadisticasOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos.ConteoPorPeriodo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarReportesOmnicanalServiceTest {

    @Mock
    RepositorioAnalisis repositorioAnalisis;
    @Mock
    RepositorioCasos repositorioCasos;

    private ConsultarReportesOmnicanalService servicio() {
        return new ConsultarReportesOmnicanalService(repositorioAnalisis, repositorioCasos);
    }

    @Test
    void estadisticas_mapea_la_serie_temporal_del_repositorio() {
        OffsetDateTime d = OffsetDateTime.now().minusDays(7);
        OffsetDateTime h = OffsetDateTime.now();
        when(repositorioCasos.contarTotal(d, h)).thenReturn(5L);
        when(repositorioCasos.serieTemporal("dia", d, h)).thenReturn(List.of(
                new ConteoPorPeriodo("2026-09-08", 2L),
                new ConteoPorPeriodo("2026-09-09", 3L)));

        EstadisticasOmnicanal e = servicio().estadisticas("dia", d, h);

        assertEquals(5L, e.totalCasos());
        assertEquals("dia", e.agrupacion());
        assertEquals(2, e.serieTemporal().size());
        assertEquals("2026-09-08", e.serieTemporal().get(0).periodo());
        assertEquals(2L, e.serieTemporal().get(0).total());
        assertEquals(3L, e.serieTemporal().get(1).total());
    }

    @Test
    void agrupacion_null_cae_a_dia() {
        when(repositorioCasos.serieTemporal(any(), any(), any())).thenReturn(List.of());

        assertEquals("dia", servicio().estadisticas(null, null, null).agrupacion());
    }
}
