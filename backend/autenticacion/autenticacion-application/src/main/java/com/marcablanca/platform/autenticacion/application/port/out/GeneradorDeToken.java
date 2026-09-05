package com.marcablanca.platform.autenticacion.application.port.out;

public interface GeneradorDeToken {
    String generarPara(DatosDeUsuario usuario, String identificadorEmpresa);
}
