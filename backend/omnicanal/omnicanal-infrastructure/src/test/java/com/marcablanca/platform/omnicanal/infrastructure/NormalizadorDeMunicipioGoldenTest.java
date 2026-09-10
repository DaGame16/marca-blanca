package com.marcablanca.platform.omnicanal.infrastructure;

import com.marcablanca.platform.omnicanal.domain.NormalizadorDeMunicipio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Golden test: con el perfil ISP, la normalizacion de municipios no cambia. */
class NormalizadorDeMunicipioGoldenTest {

    private final NormalizadorDeMunicipio normalizador =
            new NormalizadorDeMunicipio(PerfilDeAnalisisPredeterminado.ISP);

    @Test
    void fuzzy_y_abreviaturas_de_la_guajira_cesar() {
        assertEquals("Riohacha", normalizador.normalizar("rioacha"));
        assertEquals("San Juan del Cesar", normalizador.normalizar("san juan"));
        assertEquals("Agustín Codazzi", normalizador.normalizar("codazzi"));
    }

    @Test
    void valores_vacios() {
        assertNull(normalizador.normalizar("la guajira"));
        assertNull(normalizador.normalizar(null));
        assertNull(normalizador.normalizar("   "));
    }

    @Test
    void desconocido_se_capitaliza() {
        assertEquals("Springfield", normalizador.normalizar("springfield"));
    }
}
