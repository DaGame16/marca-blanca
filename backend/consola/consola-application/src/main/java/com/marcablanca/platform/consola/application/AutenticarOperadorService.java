package com.marcablanca.platform.consola.application;

import com.marcablanca.platform.consola.application.port.in.AutenticarOperador;
import com.marcablanca.platform.consola.application.port.out.CifradorDeContrasenaDeOperador;
import com.marcablanca.platform.consola.application.port.out.EmisorDeTokenDeOperador;
import com.marcablanca.platform.consola.application.port.out.RepositorioOperadores;
import com.marcablanca.platform.consola.domain.CredencialesDeOperadorInvalidasException;
import com.marcablanca.platform.consola.domain.Operador;

/**
 * Verifica correo + contrasena contra la base de control y, si todo cuadra, pide
 * un token de plataforma. Sin framework a proposito: lo conecta como bean
 * {@code ConfiguracionConsola}.
 */
public class AutenticarOperadorService implements AutenticarOperador {

    private final RepositorioOperadores repositorioOperadores;
    private final CifradorDeContrasenaDeOperador cifrador;
    private final EmisorDeTokenDeOperador emisorDeToken;

    public AutenticarOperadorService(RepositorioOperadores repositorioOperadores,
                                     CifradorDeContrasenaDeOperador cifrador,
                                     EmisorDeTokenDeOperador emisorDeToken) {
        this.repositorioOperadores = repositorioOperadores;
        this.cifrador = cifrador;
        this.emisorDeToken = emisorDeToken;
    }

    @Override
    public ResultadoLoginOperador ejecutar(String correo, String contrasena) {
        Operador operador = repositorioOperadores.buscarPorCorreo(normalizar(correo))
                .orElseThrow(CredencialesDeOperadorInvalidasException::new);

        if (!cifrador.coincide(contrasena == null ? "" : contrasena, operador.getHashContrasena())) {
            throw new CredencialesDeOperadorInvalidasException();
        }
        operador.exigirActivo();

        return new ResultadoLoginOperador(
                operador.getId(),
                operador.getCorreo(),
                operador.getRol().name(),
                emisorDeToken.emitirPara(operador),
                operador.debeCambiarContrasena());
    }

    private static String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }
}
