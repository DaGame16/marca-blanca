package com.marcablanca.platform.consola.infrastructure;

import com.marcablanca.platform.consola.application.port.in.CambiarContrasenaDeOperador;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Envuelve el servicio puro en una transaccion (la lectura del operador y el
 * guardado ocurren en la misma). Usa el {@code transactionManager} @Primary, que
 * es el de la base de control.
 */
class CambiarContrasenaDeOperadorTransaccional implements CambiarContrasenaDeOperador {

    private final CambiarContrasenaDeOperador delegado;

    CambiarContrasenaDeOperadorTransaccional(CambiarContrasenaDeOperador delegado) {
        this.delegado = delegado;
    }

    @Override
    @Transactional
    public void ejecutar(UUID operadorId, String contrasenaActual, String contrasenaNueva) {
        delegado.ejecutar(operadorId, contrasenaActual, contrasenaNueva);
    }
}
