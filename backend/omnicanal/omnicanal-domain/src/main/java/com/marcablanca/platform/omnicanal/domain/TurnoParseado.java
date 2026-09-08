package com.marcablanca.platform.omnicanal.domain;

import java.time.OffsetDateTime;

/**
 * Turno recien parseado del texto crudo, TODAVIA sin conversacionId ni
 * orden final -- eso se asigna al persistir. Se usa durante el pipeline
 * de ingesta y de analisis, nunca se guarda tal cual.
 */
public record TurnoParseado(String nombre, String fechaTexto, OffsetDateTime fecha, String mensaje,
                             AutorTurno autor) {

    public boolean esCliente() {
        return autor == AutorTurno.CLIENTE;
    }
}
