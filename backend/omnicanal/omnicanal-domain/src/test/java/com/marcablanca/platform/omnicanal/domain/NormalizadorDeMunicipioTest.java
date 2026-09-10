package com.marcablanca.platform.omnicanal.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * El algoritmo (Levenshtein con tolerancia, abreviaturas, "vacios") es fijo;
 * la lista de lugares conocidos sale del perfil.
 */
class NormalizadorDeMunicipioTest {

    private static PerfilDeAnalisisOmnicanal perfil() {
        return new PerfilDeAnalisisOmnicanal("Empresa", "s", "u %s", "https://x", "1",
                List.of(), List.of(), List.of(),
                List.of("Gotham", "Metropolis"),
                Map.of("gc", "Gotham"),
                List.of("nada"));
    }

    private final NormalizadorDeMunicipio normalizador = new NormalizadorDeMunicipio(perfil());

    @Test
    void fuzzy_contra_los_lugares_del_perfil() {
        assertEquals("Gotham", normalizador.normalizar("gotha"));
        assertEquals("Metropolis", normalizador.normalizar("metropollis"));
    }

    @Test
    void abreviatura_del_perfil() {
        assertEquals("Gotham", normalizador.normalizar("GC"));
    }

    @Test
    void valor_vacio_del_perfil_es_null() {
        assertNull(normalizador.normalizar("nada"));
    }

    @Test
    void nulo_es_null() {
        assertNull(normalizador.normalizar(null));
    }

    @Test
    void un_lugar_que_no_esta_en_este_perfil_se_devuelve_capitalizado() {
        // "riohacha" estaba fijo en el codigo viejo; con este perfil no existe.
        assertEquals("Riohacha", normalizador.normalizar("riohacha"));
    }
}
