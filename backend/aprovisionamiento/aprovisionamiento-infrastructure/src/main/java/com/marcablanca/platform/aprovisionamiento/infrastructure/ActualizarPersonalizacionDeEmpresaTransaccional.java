package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.ActualizarPersonalizacionDeEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.port.in.ActualizarPersonalizacionDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.domain.Personalizacion;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

class ActualizarPersonalizacionDeEmpresaTransaccional implements ActualizarPersonalizacionDeEmpresa {

    private final ActualizarPersonalizacionDeEmpresaService delegado;

    ActualizarPersonalizacionDeEmpresaTransaccional(ActualizarPersonalizacionDeEmpresaService delegado) {
        this.delegado = delegado;
    }

    @Override
    @Transactional
    public void ejecutar(UUID empresaId, Personalizacion personalizacion) {
        delegado.ejecutar(empresaId, personalizacion);
    }
}
