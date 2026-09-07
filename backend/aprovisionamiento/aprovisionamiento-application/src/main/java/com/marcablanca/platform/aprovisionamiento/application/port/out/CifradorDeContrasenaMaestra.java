package com.marcablanca.platform.aprovisionamiento.application.port.out;

import com.marcablanca.platform.aprovisionamiento.domain.HashContrasenaMaestra;

/** Cifra la contrasena maestra en claro. Implementacion: BCrypt en infraestructura. */
public interface CifradorDeContrasenaMaestra {
    HashContrasenaMaestra cifrar(String contrasenaEnClaro);
}