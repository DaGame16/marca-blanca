package com.marcablanca.platform.usuarios.application;

/** Puerto de entrada. La infraestructura (controller REST) depende solo de esta interfaz. */
public interface AutenticarUsuario {
    ResultadoAutenticacion ejecutar(String correo, String contrasenaPlano);
}
