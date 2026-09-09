package com.marcablanca.platform.consola.application;

import com.marcablanca.platform.consola.application.port.in.CambiarContrasenaDeOperador;
import com.marcablanca.platform.consola.application.port.out.CifradorDeContrasenaDeOperador;
import com.marcablanca.platform.consola.application.port.out.RepositorioOperadores;
import com.marcablanca.platform.consola.domain.CredencialesDeOperadorInvalidasException;
import com.marcablanca.platform.consola.domain.Operador;
import com.marcablanca.platform.consola.domain.OperadorNoEncontradoException;

import java.util.UUID;

/** Comprueba la contrasena actual, valida la nueva y la guarda ya cifrada. */
public class CambiarContrasenaDeOperadorService implements CambiarContrasenaDeOperador {

    private static final int LARGO_MINIMO = 10;

    private final RepositorioOperadores repositorioOperadores;
    private final CifradorDeContrasenaDeOperador cifrador;

    public CambiarContrasenaDeOperadorService(RepositorioOperadores repositorioOperadores,
                                              CifradorDeContrasenaDeOperador cifrador) {
        this.repositorioOperadores = repositorioOperadores;
        this.cifrador = cifrador;
    }

    @Override
    public void ejecutar(UUID operadorId, String contrasenaActual, String contrasenaNueva) {
        Operador operador = repositorioOperadores.buscarPorId(operadorId)
                .orElseThrow(() -> new OperadorNoEncontradoException(operadorId));

        if (!cifrador.coincide(contrasenaActual == null ? "" : contrasenaActual, operador.getHashContrasena())) {
            throw new CredencialesDeOperadorInvalidasException();
        }
        exigirNuevaValida(contrasenaNueva);

        operador.cambiarContrasena(cifrador.cifrar(contrasenaNueva));
        repositorioOperadores.guardar(operador);
    }

    private static void exigirNuevaValida(String nueva) {
        if (nueva == null || nueva.length() < LARGO_MINIMO) {
            throw new IllegalArgumentException(
                    "La nueva contrasena debe tener al menos " + LARGO_MINIMO + " caracteres.");
        }
    }
}
