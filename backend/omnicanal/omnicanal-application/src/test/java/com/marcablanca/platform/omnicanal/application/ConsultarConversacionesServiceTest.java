package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.ConsultarConversaciones.ResumenContacto;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.domain.AnalisisDeCaso;
import com.marcablanca.platform.omnicanal.domain.Caso;
import com.marcablanca.platform.omnicanal.domain.Conversacion;
import com.marcablanca.platform.omnicanal.domain.ConversacionNoEncontradaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarConversacionesServiceTest {

    @Mock
    RepositorioConversaciones repositorioConversaciones;
    @Mock
    RepositorioCasos repositorioCasos;
    @Mock
    RepositorioAnalisis repositorioAnalisis;

    private ConsultarConversacionesService servicio() {
        return new ConsultarConversacionesService(repositorioConversaciones, repositorioCasos, repositorioAnalisis);
    }

    private static Conversacion conversacion(long id, String contacto) {
        return new Conversacion(id, UUID.randomUUID(), contacto, "Ana", "hist", "{}", false,
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    private static Caso caso(long id, long conversacionId) {
        return new Caso(id, UUID.randomUUID(), conversacionId, 0, 3, true, false, OffsetDateTime.now());
    }

    private static AnalisisDeCaso analisisVacio(long casoId) {
        return new AnalisisDeCaso(1L, UUID.randomUUID(), casoId, "573001", null, null, null, null, null, null,
                null, null, null, null, null, null, null, List.of(), List.of(), null, null, null, null, null,
                false, "gpt-4.1-mini", "razona", null, null, null, OffsetDateTime.now());
    }

    @Test
    void contacto_sin_conversacion_lanza_no_encontrada() {
        when(repositorioConversaciones.buscarPorIdContacto("nadie")).thenReturn(Optional.empty());
        assertThrows(ConversacionNoEncontradaException.class, () -> servicio().resumenDeContacto("nadie"));
    }

    @Test
    void resumen_trae_los_casos_del_contacto_con_su_analisis() {
        when(repositorioConversaciones.buscarPorIdContacto("573001"))
                .thenReturn(Optional.of(conversacion(10L, "573001")));
        Caso c1 = caso(1L, 10L);
        Caso c2 = caso(2L, 10L);
        when(repositorioCasos.listarPorConversacion(10L)).thenReturn(List.of(c1, c2));
        AnalisisDeCaso a1 = analisisVacio(1L);
        when(repositorioAnalisis.buscarPorCasoId(1L)).thenReturn(Optional.of(a1));
        when(repositorioAnalisis.buscarPorCasoId(2L)).thenReturn(Optional.empty());

        ResumenContacto r = servicio().resumenDeContacto("573001");

        assertEquals("573001", r.idContacto());
        assertEquals("Ana", r.nombreContacto());
        assertEquals(2, r.totalCasos());
        assertSame(c1, r.casos().get(0).caso());
        assertSame(a1, r.casos().get(0).analisis());
        assertSame(c2, r.casos().get(1).caso());
        assertNull(r.casos().get(1).analisis());
    }
}
