package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.CambiarEstadoDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEncontradaException;

import java.util.UUID;

public class CambiarEstadoDeEmpresaService implements CambiarEstadoDeEmpresa {

    private final RepositorioEmpresas repositorioEmpresas;

    public CambiarEstadoDeEmpresaService(RepositorioEmpresas repositorioEmpresas) {
        this.repositorioEmpresas = repositorioEmpresas;
    }

    @Override
    public void ejecutar(UUID empresaId, Transicion transicion) {
        Empresa empresa = repositorioEmpresas.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        switch (transicion) {
            case SUSPENDER -> empresa.suspender();
            case REACTIVAR -> empresa.reactivar();
        }

        repositorioEmpresas.guardar(empresa);
    }
}
