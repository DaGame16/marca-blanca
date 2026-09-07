package com.marcablanca.platform.aprovisionamiento.infrastructure.pipeline;

import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.ActivarModuloDeEmpresa;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador Anti-Corruption Layer: traduce la necesidad de este modulo
 * (ActivadorDeModulosDeEmpresa) a una llamada al puerto de entrada publico de
 * modulos-empresa. Solo depende de su interfaz de entrada, nunca de su modelo
 * interno ni de sus tablas.
 */
@Component
class PuenteModulosEmpresa implements ActivadorDeModulosDeEmpresa {

    private final ActivarModuloDeEmpresa activarModuloDeEmpresa;

    PuenteModulosEmpresa(ActivarModuloDeEmpresa activarModuloDeEmpresa) {
        this.activarModuloDeEmpresa = activarModuloDeEmpresa;
    }

    @Override
    public void activar(UUID empresaId, String codigoModulo) {
        activarModuloDeEmpresa.ejecutar(empresaId, codigoModulo);
    }
}
