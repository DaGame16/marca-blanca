package com.marcablanca.platform.empresas.domain;

/**
 * Una conexion activa junto con el identificador de la empresa duena, para
 * los casos (ej. resolver por correo) que necesitan recorrer todas las
 * empresas activas en vez de una sola ya conocida.
 */
public record EmpresaConexionDeEmpresa(String identificadorEmpresa, EmpresaConexion conexion) {
}
