package com.marcablanca.platform.autenticacion.application.port.out;

import java.util.Set;

public interface GeneradorDeToken {
    String generarPara(DatosDeUsuario usuario, Set<String> permisos, String identificadorEmpresa);
}
