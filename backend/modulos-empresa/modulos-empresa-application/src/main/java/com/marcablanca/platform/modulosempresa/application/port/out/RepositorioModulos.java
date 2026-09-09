package com.marcablanca.platform.modulosempresa.application.port.out;

import com.marcablanca.platform.modulosempresa.domain.Modulo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioModulos {

    List<Modulo> listarTodos();

    Optional<Modulo> buscarPorId(UUID id);

    boolean existePorCodigo(String codigo);

    /** Alta o actualizacion segun exista el id. Devuelve el modulo persistido. */
    Modulo guardar(Modulo modulo);

    /** Lanza ModuloEnUsoException si alguna empresa lo tiene asignado (FK). */
    void eliminar(UUID id);
}
