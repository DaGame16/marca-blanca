package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.ComandoRegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.RegistrarEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.port.in.RegistrarEmpresa;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Decorador transaccional de la Capa 1. La capa de aplicacion queda libre de
 * Spring; aca se abre la transaccion que hace atomico "guardar empresa + escribir
 * el outbox". Usa el transactionManager @Primary (unidad de persistencia "control").
 */
class RegistrarEmpresaTransaccional implements RegistrarEmpresa {

    private final RegistrarEmpresaService delegado;

    RegistrarEmpresaTransaccional(RegistrarEmpresaService delegado) {
        this.delegado = delegado;
    }

    @Override
    @Transactional
    public UUID ejecutar(ComandoRegistrarEmpresa comando) {
        return delegado.ejecutar(comando);
    }
}