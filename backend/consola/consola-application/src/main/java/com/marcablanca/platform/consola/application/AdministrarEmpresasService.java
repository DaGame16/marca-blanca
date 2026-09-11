package com.marcablanca.platform.consola.application;

import com.marcablanca.platform.consola.application.port.in.AdministrarEmpresas;
import com.marcablanca.platform.consola.application.port.out.AdministracionDeEmpresas;
import com.marcablanca.platform.consola.application.port.out.RegistroDeAuditoria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquesta la administracion de empresas: delega en el contexto de
 * aprovisionamiento (via el puerto ACL) y deja el rastro de auditoria. Solo se
 * audita si la operacion no lanzo excepcion.
 */
public class AdministrarEmpresasService implements AdministrarEmpresas {

    static final String ACCION_SUSPENDER = "empresa.suspendida";
    static final String ACCION_REACTIVAR = "empresa.reactivada";
    static final String ACCION_DATOS = "empresa.datos_editados";
    static final String ACCION_MARCA = "empresa.marca_editada";
    static final String ACCION_MODULO_ON = "empresa.modulo_activado";
    static final String ACCION_MODULO_OFF = "empresa.modulo_desactivado";
    static final String ACCION_IA_OMNICANAL = "empresa.omnicanal_ia_cambiada";
    static final String ACCION_WEBHOOK_ROTADO = "empresa.omnicanal_webhook_rotado";

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
    public Optional<DetalleEmpresaConsola> detalle(UUID empresaId) {
        return administracionDeEmpresas.detalle(empresaId);
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

    @Override
    public void actualizarDatos(UUID operadorId, UUID empresaId, DatosEmpresaConsola datos) {
        administracionDeEmpresas.actualizarDatos(empresaId, datos);
        registroDeAuditoria.registrar(operadorId, ACCION_DATOS, empresaId);
    }

    @Override
    public void actualizarMarca(UUID operadorId, UUID empresaId, MarcaConsola marca) {
        administracionDeEmpresas.actualizarMarca(empresaId, marca);
        registroDeAuditoria.registrar(operadorId, ACCION_MARCA, empresaId);
    }

    @Override
    public void activarModulo(UUID operadorId, UUID empresaId, String codigoModulo) {
        administracionDeEmpresas.activarModulo(empresaId, codigoModulo);
        registroDeAuditoria.registrar(operadorId, ACCION_MODULO_ON, empresaId);
    }

    @Override
    public void desactivarModulo(UUID operadorId, UUID empresaId, String codigoModulo) {
        administracionDeEmpresas.desactivarModulo(empresaId, codigoModulo);
        registroDeAuditoria.registrar(operadorId, ACCION_MODULO_OFF, empresaId);
    }

    @Override
    public Optional<VistaOmnicanalConsola> verOmnicanal(UUID empresaId) {
        return administracionDeEmpresas.verOmnicanal(empresaId);
    }

    @Override
    public void establecerIaHabilitadaOmnicanal(UUID operadorId, UUID empresaId, boolean habilitada) {
        administracionDeEmpresas.establecerIaHabilitadaOmnicanal(empresaId, habilitada);
        registroDeAuditoria.registrar(operadorId, ACCION_IA_OMNICANAL, empresaId);
    }

    @Override
    public VistaOmnicanalConsola rotarWebhookSecretOmnicanal(UUID operadorId, UUID empresaId) {
        VistaOmnicanalConsola vista = administracionDeEmpresas.rotarWebhookSecretOmnicanal(empresaId);
        registroDeAuditoria.registrar(operadorId, ACCION_WEBHOOK_ROTADO, empresaId);
        return vista;
    }
}
