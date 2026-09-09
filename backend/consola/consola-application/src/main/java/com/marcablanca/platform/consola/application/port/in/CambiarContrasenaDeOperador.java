package com.marcablanca.platform.consola.application.port.in;

import java.util.UUID;

/** Caso de uso: un operador cambia su propia contrasena (obligatorio en el primer ingreso). */
public interface CambiarContrasenaDeOperador {

    void ejecutar(UUID operadorId, String contrasenaActual, String contrasenaNueva);
}
