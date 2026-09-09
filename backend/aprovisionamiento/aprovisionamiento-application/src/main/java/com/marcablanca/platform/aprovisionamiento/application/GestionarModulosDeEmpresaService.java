package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.GestionarModulosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEncontradaException;

import java.util.UUID;

public class GestionarModulosDeEmpresaService implements GestionarModulosDeEmpresa {

    private final RepositorioEmpresas repositorioEmpresas;
    private final ActivadorDeModulosDeEmpresa activadorDeModulos;

    public GestionarModulosDeEmpresaService(RepositorioEmpresas repositorioEmpresas,
                                            ActivadorDeModulosDeEmpresa activadorDeModulos) {
        this.repositorioEmpresas = repositorioEmpresas;
        this.activadorDeModulos = activadorDeModulos;
    }

    @Override
    public void activar(UUID empresaId, String codigoModulo) {
        exigirEditable(empresaId);
        activadorDeModulos.activar(empresaId, codigoModulo);
    }

    @Override
    public void desactivar(UUID empresaId, String codigoModulo) {
        exigirEditable(empresaId);
        activadorDeModulos.desactivar(empresaId, codigoModulo);
    }

    private void exigirEditable(UUID empresaId) {
        Empresa empresa = repositorioEmpresas.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));
        empresa.exigirEditablePorOperador();
    }
}
