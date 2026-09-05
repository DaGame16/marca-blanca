package com.marcablanca.platform.identidadvisual.application;

import com.marcablanca.platform.identidadvisual.application.port.in.ObtenerMarcaDeEmpresa;
import com.marcablanca.platform.identidadvisual.application.port.out.RepositorioMarcaDeEmpresa;
import com.marcablanca.platform.identidadvisual.domain.MarcaDeEmpresa;

public class ObtenerMarcaDeEmpresaService implements ObtenerMarcaDeEmpresa {

    private final RepositorioMarcaDeEmpresa repositorioMarcaDeEmpresa;

    public ObtenerMarcaDeEmpresaService(RepositorioMarcaDeEmpresa repositorioMarcaDeEmpresa) {
        this.repositorioMarcaDeEmpresa = repositorioMarcaDeEmpresa;
    }

    @Override
    public MarcaDeEmpresa ejecutar(String identificadorEmpresa) {
        return repositorioMarcaDeEmpresa.obtener(identificadorEmpresa);
    }
}
