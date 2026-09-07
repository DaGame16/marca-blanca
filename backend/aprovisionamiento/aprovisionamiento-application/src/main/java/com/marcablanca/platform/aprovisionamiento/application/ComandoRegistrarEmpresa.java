package com.marcablanca.platform.aprovisionamiento.application;

import java.util.Set;

/** Datos de entrada de la Capa 1. La validacion de forma se hace antes, en la capa web. */
public record ComandoRegistrarEmpresa(
        String identificador,
        String nombreLegal,
        String nombreComercial,
        String dominio,
        String contrasenaMaestra,
        Set<String> modulosSolicitados
) {
}