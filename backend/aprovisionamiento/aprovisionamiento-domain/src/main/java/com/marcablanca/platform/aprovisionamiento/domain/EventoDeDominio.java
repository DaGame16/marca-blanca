package com.marcablanca.platform.aprovisionamiento.domain;

import java.time.Instant;

/** Marca comun de los eventos que levanta el dominio de aprovisionamiento. */
public sealed interface EventoDeDominio permits EmpresaRegistrada {
    Instant ocurridoEn();
}