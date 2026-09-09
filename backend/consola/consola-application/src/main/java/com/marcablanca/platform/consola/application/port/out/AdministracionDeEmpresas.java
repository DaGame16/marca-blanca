package com.marcablanca.platform.consola.application.port.out;

import com.marcablanca.platform.consola.application.EmpresaParaConsola;

import java.util.List;
import java.util.UUID;

/**
 * Puerto ACL hacia el contexto de aprovisionamiento (dueno del agregado Empresa).
 * La consola nunca toca ese contexto directo: el adaptador que implementa esto
 * vive en consola.infrastructure y traduce hacia los puertos de entrada de
 * aprovisionamiento.
 */
public interface AdministracionDeEmpresas {

    List<EmpresaParaConsola> listar();

    void suspender(UUID empresaId);

    void reactivar(UUID empresaId);
}
