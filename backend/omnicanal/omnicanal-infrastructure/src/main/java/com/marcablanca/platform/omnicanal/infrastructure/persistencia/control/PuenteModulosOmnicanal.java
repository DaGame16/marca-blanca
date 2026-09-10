package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.domain.ModuloDeEmpresa;
import com.marcablanca.platform.omnicanal.application.port.out.ModuloOmnicanalHabilitado;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador ACL: responde si la empresa activa tiene el modulo "omnicanal"
 * activo, traduciendo hacia el puerto de entrada publico de modulos-empresa.
 * Mismo criterio que PuenteModulosEmpresa en aprovisionamiento.
 *
 * Vive en el paquete de persistencia de control porque necesita el mapeo
 * minimo EmpresaRefDeOmnicanal (package-private) para pasar del "identificador"
 * (slug) que hay en ContextoEmpresaActual al uuid de empresa que espera
 * ListarModulosDeEmpresa.
 */
@Component
class PuenteModulosOmnicanal implements ModuloOmnicanalHabilitado {

    private final EmpresaRefDeOmnicanalJpaRepository empresasRef;
    private final ListarModulosDeEmpresa listarModulosDeEmpresa;

    PuenteModulosOmnicanal(EmpresaRefDeOmnicanalJpaRepository empresasRef,
                           ListarModulosDeEmpresa listarModulosDeEmpresa) {
        this.empresasRef = empresasRef;
        this.listarModulosDeEmpresa = listarModulosDeEmpresa;
    }

    @Override
    public boolean paraEmpresaActual() {
        Optional<UUID> empresaUuid = ContextoEmpresaActual.obtener()
                .flatMap(empresasRef::findByIdentificador)
                .map(EmpresaRefDeOmnicanal::getUuid);
        if (empresaUuid.isEmpty()) {
            return false;
        }
        return listarModulosDeEmpresa.ejecutar(empresaUuid.get()).stream()
                .anyMatch(m -> CODIGO.equals(m.codigo()) && m.activo());
    }
}
