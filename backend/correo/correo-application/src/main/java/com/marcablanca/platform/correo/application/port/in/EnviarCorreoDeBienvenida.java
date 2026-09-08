package com.marcablanca.platform.correo.application.port.in;

public interface EnviarCorreoDeBienvenida {

    void ejecutar(ComandoBienvenida comando);

    record ComandoBienvenida(String correoDestino, String nombreContacto, String nombreEmpresa,
                              String urlSitio, String contrasenaGenerada) {
    }
}
