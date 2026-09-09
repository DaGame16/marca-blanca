package com.marcablanca.platform.aprovisionamiento.application.port.out;

import com.marcablanca.platform.aprovisionamiento.application.ModuloDisponible;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Puerto de salida hacia modulos-empresa. El adaptador (ACL) delega en los
 * puertos de entrada publicos de ese modulo, sin tocar su modelo interno.
 */
public interface ActivadorDeModulosDeEmpresa {
    void activar(UUID empresaId, String codigoModulo);
    void desactivar(UUID empresaId, String codigoModulo);
    Set<String> codigosSeleccionados(UUID empresaId);

    /** Catalogo completo con el flag activo/inactivo para esta empresa. */
    List<ModuloDisponible> listar(UUID empresaId);
}
