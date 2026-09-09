package com.marcablanca.platform.aprovisionamiento.application;

import java.util.List;

/** Vista completa de una empresa para editar: datos + marca + catalogo de modulos. */
public record DetalleDeEmpresa(
        DatosYMarcaDeEmpresa datos,
        List<ModuloDisponible> modulos) {
}
