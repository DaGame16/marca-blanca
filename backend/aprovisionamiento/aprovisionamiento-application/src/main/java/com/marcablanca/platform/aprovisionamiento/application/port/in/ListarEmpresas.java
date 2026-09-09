package com.marcablanca.platform.aprovisionamiento.application.port.in;

import com.marcablanca.platform.aprovisionamiento.application.ResumenDeEmpresa;

import java.util.List;

/** Caso de uso: listar todas las empresas para un panel de administracion. */
public interface ListarEmpresas {

    List<ResumenDeEmpresa> ejecutar();
}
