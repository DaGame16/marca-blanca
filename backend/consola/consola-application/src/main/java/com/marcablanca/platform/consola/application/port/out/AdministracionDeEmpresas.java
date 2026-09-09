package com.marcablanca.platform.consola.application.port.out;

import com.marcablanca.platform.consola.application.DatosEmpresaConsola;
import com.marcablanca.platform.consola.application.DetalleEmpresaConsola;
import com.marcablanca.platform.consola.application.EmpresaParaConsola;
import com.marcablanca.platform.consola.application.MarcaConsola;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto ACL hacia el contexto de aprovisionamiento (dueno del agregado Empresa).
 * La consola nunca toca ese contexto directo: el adaptador que implementa esto
 * vive en consola.infrastructure y traduce hacia los puertos de entrada de
 * aprovisionamiento.
 */
public interface AdministracionDeEmpresas {

    List<EmpresaParaConsola> listar();

    Optional<DetalleEmpresaConsola> detalle(UUID empresaId);

    void suspender(UUID empresaId);

    void reactivar(UUID empresaId);

    void actualizarDatos(UUID empresaId, DatosEmpresaConsola datos);

    void actualizarMarca(UUID empresaId, MarcaConsola marca);

    void activarModulo(UUID empresaId, String codigoModulo);

    void desactivarModulo(UUID empresaId, String codigoModulo);
}
