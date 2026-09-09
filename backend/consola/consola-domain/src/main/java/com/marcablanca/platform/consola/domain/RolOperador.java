package com.marcablanca.platform.consola.domain;

/**
 * Rol de un operador dentro de la consola de operacion de la plataforma. No se
 * confunde con los roles de un usuario dentro de una empresa cliente: aquel vive
 * en la base de cada tenant, este en la base de control.
 *
 * <ul>
 *   <li>{@code SUPER_ADMIN}: todo, incluido dar de alta/baja a otros operadores
 *       y las acciones irreversibles sobre bases de cliente.</li>
 *   <li>{@code SOPORTE}: lectura y acciones no destructivas (reenviar bienvenida,
 *       re-aprovisionar).</li>
 * </ul>
 */
public enum RolOperador {
    SUPER_ADMIN,
    SOPORTE
}
