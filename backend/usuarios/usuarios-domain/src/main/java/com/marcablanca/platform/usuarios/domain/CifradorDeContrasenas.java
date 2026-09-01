package com.marcablanca.platform.usuarios.domain;

public interface CifradorDeContrasenas {
    HashContrasena cifrar(Contrasena contrasena);
    boolean verificar(Contrasena contrasenaCandidata, HashContrasena hashAlmacenado);
}
