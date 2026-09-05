package com.marcablanca.platform.empresas.application.port.in;

import com.marcablanca.platform.empresas.domain.ModuloDeEmpresa;

import java.util.List;
import java.util.UUID;

public interface ListarModulosDeEmpresa {
    List<ModuloDeEmpresa> ejecutar(UUID empresaId);
}
