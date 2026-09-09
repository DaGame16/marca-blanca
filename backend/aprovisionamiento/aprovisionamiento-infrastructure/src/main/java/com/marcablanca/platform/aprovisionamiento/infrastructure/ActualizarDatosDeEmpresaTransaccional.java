package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.ActualizarDatosDeEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.DatosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.ActualizarDatosDeEmpresa;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

class ActualizarDatosDeEmpresaTransaccional implements ActualizarDatosDeEmpresa {

    private final ActualizarDatosDeEmpresaService delegado;

    ActualizarDatosDeEmpresaTransaccional(ActualizarDatosDeEmpresaService delegado) {
        this.delegado = delegado;
    }

    @Override
    @Transactional
    public void ejecutar(UUID empresaId, DatosDeEmpresa datos) {
        delegado.ejecutar(empresaId, datos);
    }
}
