package com.marcablanca.platform.empresas.application.port.in;

import java.util.Optional;

/**
 * Puente para pantallas publicas (ej. el login, antes de haber sesion) que
 * necesitan mostrar el nombre real de la empresa -- no solo su logo/colores
 * -- a partir del identificador del subdominio.
 */
public interface ObtenerNombreDeEmpresa {
    Optional<String> ejecutar(String identificadorEmpresa);
}
