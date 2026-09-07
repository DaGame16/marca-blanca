package com.marcablanca.platform.aprovisionamiento.application.port.out;

import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.Identificador;

import java.util.Optional;
import java.util.UUID;

/** Persistencia del agregado Empresa en la base de control. Implementacion JPA en infraestructura. */
public interface RepositorioEmpresas {

    void guardar(Empresa empresa);

    boolean existePorIdentificador(Identificador identificador);

    boolean existePorDominio(String dominio);

    Optional<Empresa> buscarPorId(UUID id);
}