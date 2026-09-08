package com.marcablanca.platform.aprovisionamiento.infrastructure.pipeline;

import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.ActivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.DesactivarModuloDeEmpresa;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Adaptador ACL: traduce hacia los puertos de entrada publicos de modulos-empresa. */
@Component
class PuenteModulosEmpresa implements ActivadorDeModulosDeEmpresa {

    private final ActivarModuloDeEmpresa activar;
    private final DesactivarModuloDeEmpresa desactivar;

    PuenteModulosEmpresa(ActivarModuloDeEmpresa activar, DesactivarModuloDeEmpresa desactivar) {
        this.activar = activar;
        this.desactivar = desactivar;
    }

    @Override
    public void activar(UUID empresaId, String codigoModulo) {
        activar.ejecutar(empresaId, codigoModulo);
    }

    @Override
    public void desactivar(UUID empresaId, String codigoModulo) {
        desactivar.ejecutar(empresaId, codigoModulo);
    }
}