package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.IngestarConversacionArchivada.Ingesta;
import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.domain.Caso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecibirConversacionArchivadaServiceTest {

    @Mock
    IngestarConversacionArchivada ingestar;
    @Mock
    AnalizadorDeConversacion analizador;
    @Mock
    RepositorioAnalisisEscritor escritor;
    @Mock
    RepositorioConversaciones repositorioConversaciones;
    @Mock
    RepositorioCasos repositorioCasos;

    private RecibirConversacionArchivadaService servicio() {
        return new RecibirConversacionArchivadaService(ingestar, analizador, escritor,
                repositorioConversaciones, repositorioCasos);
    }

    private static Caso caso(long id) {
        return new Caso(id, UUID.randomUUID(), 1L, 0, 3, false, false, OffsetDateTime.now());
    }

    @Test
    void analiza_cada_caso_nuevo_con_el_id_de_contacto() {
        Caso c1 = caso(1L);
        Caso c2 = caso(2L);
        when(ingestar.ejecutar(any())).thenReturn(new Ingesta("573001", List.of(c1, c2)));

        servicio().ejecutar(Map.of());

        verify(escritor).analizarYGuardar(eq(c1), any(), any(), any(), eq("573001"));
        verify(escritor).analizarYGuardar(eq(c2), any(), any(), any(), eq("573001"));
    }

    @Test
    void un_caso_que_falla_no_frena_a_los_demas_ni_propaga() {
        Caso c1 = caso(1L);
        Caso c2 = caso(2L);
        when(ingestar.ejecutar(any())).thenReturn(new Ingesta("x", List.of(c1, c2)));
        doThrow(new RuntimeException("openai caido"))
                .when(escritor).analizarYGuardar(eq(c1), any(), any(), any(), any());

        servicio().ejecutar(Map.of()); // no lanza

        verify(escritor).analizarYGuardar(eq(c2), any(), any(), any(), any());
    }

    @Test
    void sin_casos_nuevos_no_llama_al_analizador() {
        when(ingestar.ejecutar(any())).thenReturn(new Ingesta("x", List.of()));

        servicio().ejecutar(Map.of());

        verify(escritor, never()).analizarYGuardar(any(), any(), any(), any(), any());
        verifyNoInteractions(analizador);
    }
}
