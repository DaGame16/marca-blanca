package com.marcablanca.platform.aprovisionamiento.application.port.out;

import com.marcablanca.platform.aprovisionamiento.application.ResumenDeEmpresa;

import java.util.List;

/**
 * Lectura directa del catalogo de empresas para paneles de administracion.
 * Separada de {@link RepositorioEmpresas} (que rehidrata el agregado) porque un
 * listado no necesita cargar aggregates completos. Implementacion JDBC en
 * infraestructura.
 */
public interface ConsultaDeEmpresas {

    List<ResumenDeEmpresa> listarTodas();
}
