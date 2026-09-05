package com.marcablanca.platform.modulosempresa.application.port.in;

import com.marcablanca.platform.modulosempresa.domain.ModuloDeEmpresa;

import java.util.List;
import java.util.UUID;

public interface ListarModulosDeEmpresa {
    List<ModuloDeEmpresa> ejecutar(UUID empresaId);
}
