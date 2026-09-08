package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.PersonalizarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioPersonalizacion;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEncontradaException;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoModificableException;
import com.marcablanca.platform.aprovisionamiento.domain.EstadoEmpresa;
import com.marcablanca.platform.aprovisionamiento.domain.Personalizacion;

import java.util.UUID;

/** Pasos 3-5: solo permite personalizar mientras la empresa esta en BORRADOR. */
public class PersonalizarEmpresaService implements PersonalizarEmpresa {

    private final RepositorioEmpresas repositorioEmpresas;
    private final RepositorioPersonalizacion repositorioPersonalizacion;

    public PersonalizarEmpresaService(RepositorioEmpresas repositorioEmpresas,
                                      RepositorioPersonalizacion repositorioPersonalizacion) {
        this.repositorioEmpresas = repositorioEmpresas;
        this.repositorioPersonalizacion = repositorioPersonalizacion;
    }

    @Override
    public void ejecutar(UUID empresaId, Personalizacion personalizacion) {
        Empresa empresa = repositorioEmpresas.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));
        if (empresa.getEstado() != EstadoEmpresa.BORRADOR) {
            throw new EmpresaNoModificableException(empresa.getEstado());
        }
        repositorioPersonalizacion.guardar(empresaId, personalizacion);
    }
}
