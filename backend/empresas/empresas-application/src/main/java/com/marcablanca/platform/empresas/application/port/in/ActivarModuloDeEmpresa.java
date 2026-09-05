package com.marcablanca.platform.empresas.application.port.in;

import java.util.UUID;

public interface ActivarModuloDeEmpresa {
    void ejecutar(UUID empresaId, String codigoModulo);
}
