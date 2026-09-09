package com.marcablanca.platform.consola.application.port.in;

import com.marcablanca.platform.consola.application.EmpresaParaConsola;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: administracion de empresas desde la consola. Cada mutacion recibe
 * el id del operador que la ejecuta para dejarla auditada.
 */
public interface AdministrarEmpresas {

    List<EmpresaParaConsola> listar();

    void suspender(UUID operadorId, UUID empresaId);

    void reactivar(UUID operadorId, UUID empresaId);
}
