package com.marcablanca.platform.consola.application.port.out;

import com.marcablanca.platform.consola.domain.Operador;

import java.util.Optional;
import java.util.UUID;

/** Persistencia de operadores. La implementa un adaptador contra la base de control. */
public interface RepositorioOperadores {

    Optional<Operador> buscarPorCorreo(String correo);

    Optional<Operador> buscarPorId(UUID id);

    void guardar(Operador operador);
}
