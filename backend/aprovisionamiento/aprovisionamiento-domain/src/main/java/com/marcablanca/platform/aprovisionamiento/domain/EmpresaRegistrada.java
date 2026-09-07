package com.marcablanca.platform.aprovisionamiento.domain;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * La empresa quedo registrada y pendiente de aprovisionamiento.
 * Este es el evento que la Capa 2 (pipeline) consume desde el outbox.
 */
public record EmpresaRegistrada(
        UUID empresaId,
        String identificador,
        String nombreLegal,
        String nombreComercial,
        String dominio,
        Set<String> modulosSolicitados,
        Instant ocurridoEn
) implements EventoDeDominio {

    public EmpresaRegistrada {
        modulosSolicitados = modulosSolicitados == null ? Set.of() : Set.copyOf(modulosSolicitados);
    }
}