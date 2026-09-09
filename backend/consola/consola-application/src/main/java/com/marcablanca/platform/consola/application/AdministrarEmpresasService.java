package com.marcablanca.platform.consola.application;

import com.marcablanca.platform.consola.application.port.in.AdministrarEmpresas;
import com.marcablanca.platform.consola.application.port.out.AdministracionDeEmpresas;
import com.marcablanca.platform.consola.application.port.out.RegistroDeAuditoria;

import java.util.List;
import java.util.UUID;

/**
 * Orquesta la administracion de empresas: delega la transicion en el contexto de
 * aprovisionamiento (via el puerto ACL) y deja el rastro de auditoria. Solo se
 * audita si la transicion no lanzo excepcion.
 */
public class AdministrarEmpresasService implements AdministrarEmpresas {

    static final String ACCION_SUSPENDER = "empresa.suspendida";
    static final String ACCION_REACTIVAR = "empresa.reactivada";

    private final AdministracionDeEmpresas administracionDeEmpresas;
    private final RegistroDeAuditoria registroDeAuditoria;

    public AdministrarEmpresasService(AdministracionDeEmpresas administracionDeEmpresas,
                                      RegistroDeAuditoria registroDeAuditoria) {
        this.administracionDeEmpresas = administracionDeEmpresas;
        this.registroDeAuditoria = registroDeAuditoria;
    }

    @Override
    public List<EmpresaParaConsola> listar() {
        return administracionDeEmpresas.listar();
    }

    @Override
    public void suspender(UUID operadorId, UUID empresaId) {
        administracionDeEmpresas.suspender(empresaId);
        registroDeAuditoria.registrar(operadorId, ACCION_SUSPENDER, empresaId);
    }

    @Override
    public void reactivar(UUID operadorId, UUID empresaId) {
        administracionDeEmpresas.reactivar(empresaId);
        registroDeAuditoria.registrar(operadorId, ACCION_REACTIVAR, empresaId);
    }
}
