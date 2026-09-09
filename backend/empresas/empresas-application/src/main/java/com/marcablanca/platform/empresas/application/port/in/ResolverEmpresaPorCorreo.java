package com.marcablanca.platform.empresas.application.port.in;

import java.util.Optional;

/**
 * Para que el login no obligue a escribir el identificador de la empresa:
 * dado un correo, busca a que empresa activa pertenece. Como cada empresa
 * vive en su propia base de datos y no hay (todavia) un directorio central
 * correo->empresa, la implementacion recorre las conexiones activas -- ver
 * el comentario en la clase de infraestructura para el detalle y las
 * limitaciones de ese enfoque.
 */
public interface ResolverEmpresaPorCorreo {
    Optional<String> ejecutar(String correo);
}
