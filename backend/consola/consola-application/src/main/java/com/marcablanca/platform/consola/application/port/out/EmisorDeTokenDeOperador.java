package com.marcablanca.platform.consola.application.port.out;

import com.marcablanca.platform.consola.domain.Operador;

/**
 * Emite el token de sesion de un operador. El adaptador produce un JWT con
 * {@code scope=plataforma} y sin claim de empresa, distinto del token de tenant.
 */
public interface EmisorDeTokenDeOperador {

    String emitirPara(Operador operador);
}
