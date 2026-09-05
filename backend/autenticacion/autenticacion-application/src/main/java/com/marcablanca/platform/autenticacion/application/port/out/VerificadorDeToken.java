package com.marcablanca.platform.autenticacion.application.port.out;

import java.util.Optional;

public interface VerificadorDeToken {
    Optional<UsuarioAutenticado> verificar(String token);
}
