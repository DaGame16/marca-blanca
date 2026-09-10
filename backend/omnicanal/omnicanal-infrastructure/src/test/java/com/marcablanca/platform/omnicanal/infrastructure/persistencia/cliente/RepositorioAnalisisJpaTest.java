package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.marcablanca.platform.omnicanal.domain.AbandonadoPor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RepositorioAnalisisJpaTest {

    @Test
    void aAbandonadoPor_mapea_los_valores_de_la_api() {
        assertEquals(AbandonadoPor.ASESOR, RepositorioAnalisisJpa.aAbandonadoPor("asesor"));
        assertEquals(AbandonadoPor.CLIENTE, RepositorioAnalisisJpa.aAbandonadoPor("  CLIENTE "));
    }

    @Test
    void aAbandonadoPor_ignora_nulo_vacio_y_desconocido() {
        assertNull(RepositorioAnalisisJpa.aAbandonadoPor(null));
        assertNull(RepositorioAnalisisJpa.aAbandonadoPor("  "));
        assertNull(RepositorioAnalisisJpa.aAbandonadoPor("nadie"));
    }
}
