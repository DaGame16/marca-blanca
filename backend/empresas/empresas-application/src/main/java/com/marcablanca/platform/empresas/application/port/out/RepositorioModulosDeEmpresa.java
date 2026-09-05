package com.marcablanca.platform.empresas.application.port.out;

import com.marcablanca.platform.empresas.domain.ModuloDeEmpresa;

import java.util.List;
import java.util.UUID;

public interface RepositorioModulosDeEmpresa {

    List<ModuloDeEmpresa> listarPorEmpresa(UUID empresaId);

    void activar(UUID empresaId, String codigoModulo);

    void desactivar(UUID empresaId, String codigoModulo);
}
