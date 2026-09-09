package com.marcablanca.platform.consola.infrastructure.aprovisionamiento;

import com.marcablanca.platform.aprovisionamiento.application.ResumenDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.CambiarEstadoDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.ListarEmpresas;
import com.marcablanca.platform.consola.application.EmpresaParaConsola;
import com.marcablanca.platform.consola.application.port.out.AdministracionDeEmpresas;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Anti-Corruption Layer entre la consola y el contexto de aprovisionamiento. Es
 * el UNICO punto donde la consola conoce los puertos de aprovisionamiento; el
 * resto trabaja contra {@link AdministracionDeEmpresas}.
 */
@Component
class PuenteAdministracionDeEmpresas implements AdministracionDeEmpresas {

    private final ListarEmpresas listarEmpresas;
    private final CambiarEstadoDeEmpresa cambiarEstadoDeEmpresa;

    PuenteAdministracionDeEmpresas(ListarEmpresas listarEmpresas,
                                   CambiarEstadoDeEmpresa cambiarEstadoDeEmpresa) {
        this.listarEmpresas = listarEmpresas;
        this.cambiarEstadoDeEmpresa = cambiarEstadoDeEmpresa;
    }

    @Override
    public List<EmpresaParaConsola> listar() {
        return listarEmpresas.ejecutar().stream().map(PuenteAdministracionDeEmpresas::traducir).toList();
    }

    @Override
    public void suspender(UUID empresaId) {
        cambiarEstadoDeEmpresa.ejecutar(empresaId, CambiarEstadoDeEmpresa.Transicion.SUSPENDER);
    }

    @Override
    public void reactivar(UUID empresaId) {
        cambiarEstadoDeEmpresa.ejecutar(empresaId, CambiarEstadoDeEmpresa.Transicion.REACTIVAR);
    }

    private static EmpresaParaConsola traducir(ResumenDeEmpresa r) {
        return new EmpresaParaConsola(
                r.id().toString(),
                r.identificador(),
                r.nombreLegal(),
                r.dominio(),
                r.correo(),
                r.estado(),
                r.pasoAprovisionamiento(),
                r.estadoTarea(),
                r.creadaEn());
    }
}
