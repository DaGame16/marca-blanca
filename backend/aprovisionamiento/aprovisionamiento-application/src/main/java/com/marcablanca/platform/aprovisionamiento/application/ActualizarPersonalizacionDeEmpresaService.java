package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.ActualizarPersonalizacionDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioPersonalizacion;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEncontradaException;
import com.marcablanca.platform.aprovisionamiento.domain.Personalizacion;

import java.util.UUID;

public class ActualizarPersonalizacionDeEmpresaService implements ActualizarPersonalizacionDeEmpresa {

    private final RepositorioEmpresas repositorioEmpresas;
    private final RepositorioPersonalizacion repositorioPersonalizacion;

    public ActualizarPersonalizacionDeEmpresaService(RepositorioEmpresas repositorioEmpresas,
                                                     RepositorioPersonalizacion repositorioPersonalizacion) {
        this.repositorioEmpresas = repositorioEmpresas;
        this.repositorioPersonalizacion = repositorioPersonalizacion;
    }

    @Override
    public void ejecutar(UUID empresaId, Personalizacion personalizacion) {
        Empresa empresa = repositorioEmpresas.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));
        empresa.exigirEditablePorOperador();
        repositorioPersonalizacion.guardar(empresaId, personalizacion);
    }
}
