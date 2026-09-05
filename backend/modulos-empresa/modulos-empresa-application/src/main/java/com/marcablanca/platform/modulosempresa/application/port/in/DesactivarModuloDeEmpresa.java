package com.marcablanca.platform.modulosempresa.application.port.in;

import java.util.UUID;

public interface DesactivarModuloDeEmpresa {
    void ejecutar(UUID empresaId, String codigoModulo);
}
