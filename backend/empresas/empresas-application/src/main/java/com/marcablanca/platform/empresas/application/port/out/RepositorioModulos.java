package com.marcablanca.platform.empresas.application.port.out;

import com.marcablanca.platform.empresas.domain.Modulo;

import java.util.List;

public interface RepositorioModulos {
    List<Modulo> listarTodos();
}
