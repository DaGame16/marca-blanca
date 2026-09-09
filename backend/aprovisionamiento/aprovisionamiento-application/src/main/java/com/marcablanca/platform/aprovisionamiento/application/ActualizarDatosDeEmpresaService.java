package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.ActualizarDatosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEncontradaException;

import java.util.UUID;

public class ActualizarDatosDeEmpresaService implements ActualizarDatosDeEmpresa {

    private final RepositorioEmpresas repositorioEmpresas;

    public ActualizarDatosDeEmpresaService(RepositorioEmpresas repositorioEmpresas) {
        this.repositorioEmpresas = repositorioEmpresas;
    }

    @Override
    public void ejecutar(UUID empresaId, DatosDeEmpresa datos) {
        Empresa empresa = repositorioEmpresas.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        empresa.actualizarDatos(
                datos.nombreLegal(), datos.representanteLegal(), datos.correo(),
                datos.telefono(), datos.sitioWeb());

        repositorioEmpresas.guardar(empresa);
    }
}
