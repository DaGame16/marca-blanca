package com.marcablanca.platform.aprovisionamiento.infrastructure.pipeline;

import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.ActivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.DesactivarModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.domain.ModuloDeEmpresa;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/** Adaptador ACL: traduce hacia los puertos de entrada publicos de modulos-empresa. */
@Component
class PuenteModulosEmpresa implements ActivadorDeModulosDeEmpresa {

    private final ActivarModuloDeEmpresa activar;
    private final DesactivarModuloDeEmpresa desactivar;
    private final ListarModulosDeEmpresa listar;

    PuenteModulosEmpresa(ActivarModuloDeEmpresa activar,
                         DesactivarModuloDeEmpresa desactivar,
                         ListarModulosDeEmpresa listar) {
        this.activar = activar;
        this.desactivar = desactivar;
        this.listar = listar;
    }

    @Override
    public void activar(UUID empresaId, String codigoModulo) {
        activar.ejecutar(empresaId, codigoModulo);
    }

    @Override
    public void desactivar(UUID empresaId, String codigoModulo) {
        desactivar.ejecutar(empresaId, codigoModulo);
    }

    @Override
    public Set<String> codigosSeleccionados(UUID empresaId) {
        Set<String> codigos = new LinkedHashSet<>();
        for (ModuloDeEmpresa m : listar.ejecutar(empresaId)) {
            if (m.activo()) {
                codigos.add(m.codigo());
            }
        }
        return codigos;
    }
}
