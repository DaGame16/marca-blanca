package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.ComandoRegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.RegistrarEmpresaService;
import com.marcablanca.platform.aprovisionamiento.application.ResultadoRegistroEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.RegistrarEmpresa;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decorador transaccional del paso 1. La capa de aplicacion queda libre de Spring;
 * aca se abre la transaccion. Usa el transactionManager @Primary (unidad "control").
 */
class RegistrarEmpresaTransaccional implements RegistrarEmpresa {

    private final RegistrarEmpresaService delegado;

    RegistrarEmpresaTransaccional(RegistrarEmpresaService delegado) {
        this.delegado = delegado;
    }

    @Override
    @Transactional
    public ResultadoRegistroEmpresa ejecutar(ComandoRegistrarEmpresa comando) {
        return delegado.ejecutar(comando);
    }
}
