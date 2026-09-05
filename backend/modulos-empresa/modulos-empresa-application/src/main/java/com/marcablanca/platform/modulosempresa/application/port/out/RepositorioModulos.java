package com.marcablanca.platform.modulosempresa.application.port.out;

import com.marcablanca.platform.modulosempresa.domain.Modulo;

import java.util.List;

public interface RepositorioModulos {
    List<Modulo> listarTodos();
}
