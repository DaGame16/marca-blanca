package com.marcablanca.platform.identidadvisual.application;

import com.marcablanca.platform.identidadvisual.application.port.in.ActualizarMarcaDeEmpresa;
import com.marcablanca.platform.identidadvisual.application.port.out.RepositorioMarcaDeEmpresa;
import com.marcablanca.platform.identidadvisual.domain.MarcaDeEmpresa;

public class ActualizarMarcaDeEmpresaService implements ActualizarMarcaDeEmpresa {

    private final RepositorioMarcaDeEmpresa repositorioMarcaDeEmpresa;

    public ActualizarMarcaDeEmpresaService(RepositorioMarcaDeEmpresa repositorioMarcaDeEmpresa) {
        this.repositorioMarcaDeEmpresa = repositorioMarcaDeEmpresa;
    }

    @Override
    public void ejecutar(String identificadorEmpresa, MarcaDeEmpresa marca) {
        repositorioMarcaDeEmpresa.guardar(identificadorEmpresa, marca);
    }
}
