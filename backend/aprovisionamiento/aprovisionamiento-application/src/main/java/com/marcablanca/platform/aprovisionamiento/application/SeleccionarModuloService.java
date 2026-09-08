package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.SeleccionarModulo;
import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEncontradaException;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoModificableException;
import com.marcablanca.platform.aprovisionamiento.domain.EstadoEmpresa;

import java.util.UUID;

public class SeleccionarModuloService implements SeleccionarModulo {

    private final RepositorioEmpresas repositorioEmpresas;
    private final ActivadorDeModulosDeEmpresa modulos;

    public SeleccionarModuloService(RepositorioEmpresas repositorioEmpresas,
                                    ActivadorDeModulosDeEmpresa modulos) {
        this.repositorioEmpresas = repositorioEmpresas;
        this.modulos = modulos;
    }

    @Override
    public void activar(UUID empresaId, String codigoModulo) {
        exigirBorrador(empresaId);
        modulos.activar(empresaId, codigoModulo);
    }

    @Override
    public void desactivar(UUID empresaId, String codigoModulo) {
        exigirBorrador(empresaId);
        modulos.desactivar(empresaId, codigoModulo);
    }

    private void exigirBorrador(UUID empresaId) {
        Empresa empresa = repositorioEmpresas.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));
        if (empresa.getEstado() != EstadoEmpresa.BORRADOR) {
            throw new EmpresaNoModificableException(empresa.getEstado());
        }
    }
}