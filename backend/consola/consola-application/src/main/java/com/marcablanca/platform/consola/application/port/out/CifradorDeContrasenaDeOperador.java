package com.marcablanca.platform.consola.application.port.out;

/** Cifrado y verificacion de contrasenas de operador. Lo implementa un adaptador BCrypt. */
public interface CifradorDeContrasenaDeOperador {

    String cifrar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String hash);
}
