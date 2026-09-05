package com.marcablanca.platform.autenticacion.application.port.in;

import com.marcablanca.platform.autenticacion.application.ResultadoAutenticacion;

public interface RenovarToken {
    ResultadoAutenticacion ejecutar(String refreshTokenActual);
}
