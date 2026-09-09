package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.ObtenerDetalleDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.ConsultaDeEmpresas;

import java.util.Optional;
import java.util.UUID;

public class ObtenerDetalleDeEmpresaService implements ObtenerDetalleDeEmpresa {

    private final ConsultaDeEmpresas consultaDeEmpresas;
    private final ActivadorDeModulosDeEmpresa activadorDeModulos;

    public ObtenerDetalleDeEmpresaService(ConsultaDeEmpresas consultaDeEmpresas,
                                          ActivadorDeModulosDeEmpresa activadorDeModulos) {
        this.consultaDeEmpresas = consultaDeEmpresas;
        this.activadorDeModulos = activadorDeModulos;
    }

    @Override
    public Optional<DetalleDeEmpresa> ejecutar(UUID empresaId) {
        return consultaDeEmpresas.datosYMarca(empresaId)
                .map(datos -> new DetalleDeEmpresa(datos, activadorDeModulos.listar(empresaId)));
    }
}
