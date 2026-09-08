package com.marcablanca.platform.correo.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DireccionCorreoTest {

    @Test
    void acepta_un_correo_valido_y_lo_normaliza_a_minuscula() {
        DireccionCorreo correo = new DireccionCorreo("Prueba@Ejemplo.com");
        assertEquals("prueba@ejemplo.com", correo.valor());
    }

    @Test
    void rechaza_sin_arroba() {
        assertThrows(DireccionCorreoInvalidaException.class, () -> new DireccionCorreo("pruebaejemplo.com"));
    }

    @Test
    void rechaza_nulo() {
        assertThrows(DireccionCorreoInvalidaException.class, () -> new DireccionCorreo(null));
    }

    @Test
    void rechaza_vacio() {
        assertThrows(DireccionCorreoInvalidaException.class, () -> new DireccionCorreo(""));
    }

    @Test
    void rechaza_sin_dominio_con_punto() {
        assertThrows(DireccionCorreoInvalidaException.class, () -> new DireccionCorreo("prueba@ejemplo"));
    }
}
