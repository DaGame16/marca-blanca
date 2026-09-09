package com.marcablanca.platform.modulosempresa.application.port.in;

import com.marcablanca.platform.modulosempresa.domain.Modulo;

import java.math.BigDecimal;
import java.util.UUID;

/** CRUD del catalogo de modulos de la plataforma (tbl_modulos). Listado: {@link ListarModulos}. */
public interface GestionarCatalogoDeModulos {

    record ComandoModulo(String codigo, String nombre, String descripcion, BigDecimal precio, String moneda) {
    }

    Modulo crear(ComandoModulo comando);

    Modulo actualizar(UUID id, ComandoModulo comando);

    void eliminar(UUID id);
}
