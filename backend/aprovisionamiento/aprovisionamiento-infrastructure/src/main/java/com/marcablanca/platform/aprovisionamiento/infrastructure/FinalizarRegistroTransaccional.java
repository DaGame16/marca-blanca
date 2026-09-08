package com.marcablanca.platform.aprovisionamiento.infrastructure;

import com.marcablanca.platform.aprovisionamiento.application.FinalizarRegistroService;
import com.marcablanca.platform.aprovisionamiento.application.ResultadoFinalizarRegistro;
import com.marcablanca.platform.aprovisionamiento.application.port.in.FinalizarRegistro;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Decorador transaccional: guardar empresa + escribir el outbox de forma atomica. */
class FinalizarRegistroTransaccional implements FinalizarRegistro {

    private final FinalizarRegistroService delegado;

    FinalizarRegistroTransaccional(FinalizarRegistroService delegado) {
        this.delegado = delegado;
    }

    @Override
    @Transactional
    public ResultadoFinalizarRegistro ejecutar(UUID empresaId) {
        return delegado.ejecutar(empresaId);
    }
}
