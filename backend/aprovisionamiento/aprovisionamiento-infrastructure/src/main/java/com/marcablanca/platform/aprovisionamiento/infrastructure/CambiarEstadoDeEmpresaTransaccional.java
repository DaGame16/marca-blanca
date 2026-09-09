package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.CambiarEstadoDeEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.port.in.CambiarEstadoDeEmpresa;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Decorador transaccional: la lectura del agregado y su guardado ocurren en la
 * misma transaccion (unidad "control", transactionManager @Primary).
 */
class CambiarEstadoDeEmpresaTransaccional implements CambiarEstadoDeEmpresa {

    private final CambiarEstadoDeEmpresaService delegado;

    CambiarEstadoDeEmpresaTransaccional(CambiarEstadoDeEmpresaService delegado) {
        this.delegado = delegado;
    }

    @Override
    @Transactional
    public void ejecutar(UUID empresaId, Transicion transicion) {
        delegado.ejecutar(empresaId, transicion);
    }
}
