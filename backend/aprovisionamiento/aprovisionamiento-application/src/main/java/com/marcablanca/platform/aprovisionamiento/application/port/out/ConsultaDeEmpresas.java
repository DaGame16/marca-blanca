package com.marcablanca.platform.aprovisionamiento.application.port.out;

import com.marcablanca.platform.aprovisionamiento.application.DatosYMarcaDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.ResumenDeEmpresa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lectura directa del catalogo de empresas para paneles de administracion.
 * Separada de {@link RepositorioEmpresas} (que rehidrata el agregado) porque un
 * listado no necesita cargar aggregates completos. Implementacion JDBC en
 * infraestructura.
 */
public interface ConsultaDeEmpresas {

    List<ResumenDeEmpresa> listarTodas();

    Optional<DatosYMarcaDeEmpresa> datosYMarca(UUID empresaId);
}
