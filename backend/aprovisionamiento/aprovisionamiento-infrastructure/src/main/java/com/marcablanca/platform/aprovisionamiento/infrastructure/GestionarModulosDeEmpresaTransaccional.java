package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.GestionarModulosDeEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.port.in.GestionarModulosDeEmpresa;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

class GestionarModulosDeEmpresaTransaccional implements GestionarModulosDeEmpresa {

    private final GestionarModulosDeEmpresaService delegado;

    GestionarModulosDeEmpresaTransaccional(GestionarModulosDeEmpresaService delegado) {
        this.delegado = delegado;
    }

    @Override
    @Transactional
    public void activar(UUID empresaId, String codigoModulo) {
        delegado.activar(empresaId, codigoModulo);
    }

    @Override
    @Transactional
    public void desactivar(UUID empresaId, String codigoModulo) {
        delegado.desactivar(empresaId, codigoModulo);
    }
}
