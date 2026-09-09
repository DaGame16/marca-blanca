package com.marcablanca.platform.consola.application.port.in;

import com.marcablanca.platform.consola.application.ResultadoLoginOperador;

/** Caso de uso: login de un operador de la plataforma. Sin identificador de empresa. */
public interface AutenticarOperador {

    ResultadoLoginOperador ejecutar(String correo, String contrasena);
}
