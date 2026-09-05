package com.marcablanca.platform.modulosempresa.application.port.in;

import java.util.UUID;

public interface ActivarModuloDeEmpresa {
    void ejecutar(UUID empresaId, String codigoModulo);
}
