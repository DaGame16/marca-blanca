package com.marcablanca.platform.autenticacion.application.port.out;

import com.marcablanca.platform.usuarios.domain.Usuario;

public interface GeneradorDeToken {
    String generarPara(Usuario usuario);
}
