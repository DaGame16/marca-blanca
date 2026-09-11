package com.marcablanca.platform.consola.application.port.in;

import com.marcablanca.platform.consola.application.DatosEmpresaConsola;
import com.marcablanca.platform.consola.application.DetalleEmpresaConsola;
import com.marcablanca.platform.consola.application.EmpresaParaConsola;
import com.marcablanca.platform.consola.application.MarcaConsola;
import com.marcablanca.platform.consola.application.VistaOmnicanalConsola;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso: administracion de empresas desde la consola. Cada mutacion recibe
 * el id del operador que la ejecuta para dejarla auditada.
 */
public interface AdministrarEmpresas {

    List<EmpresaParaConsola> listar();

    Optional<DetalleEmpresaConsola> detalle(UUID empresaId);

    void suspender(UUID operadorId, UUID empresaId);

    void reactivar(UUID operadorId, UUID empresaId);

    void actualizarDatos(UUID operadorId, UUID empresaId, DatosEmpresaConsola datos);

    void actualizarMarca(UUID operadorId, UUID empresaId, MarcaConsola marca);

    void activarModulo(UUID operadorId, UUID empresaId, String codigoModulo);

    void desactivarModulo(UUID operadorId, UUID empresaId, String codigoModulo);

    Optional<VistaOmnicanalConsola> verOmnicanal(UUID empresaId);

    void establecerIaHabilitadaOmnicanal(UUID operadorId, UUID empresaId, boolean habilitada);

    VistaOmnicanalConsola rotarWebhookSecretOmnicanal(UUID operadorId, UUID empresaId);
}
