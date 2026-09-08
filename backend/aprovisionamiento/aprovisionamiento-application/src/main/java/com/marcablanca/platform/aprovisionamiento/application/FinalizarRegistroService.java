package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.FinalizarRegistro;
import com.marcablanca.platform.aprovisionamiento.application.port.out.ActivadorDeModulosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RegistroDeEventos;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaNoEncontradaException;

import java.util.Set;
import java.util.UUID;

/**
 * Paso 6. Pasa la empresa de BORRADOR a PENDIENTE_APROVISIONAMIENTO y escribe el
 * evento de outbox (misma transaccion, via el decorador de infraestructura). El
 * sondeador toma ese evento y el pipeline aprovisiona.
 */
public class FinalizarRegistroService implements FinalizarRegistro {

    private final RepositorioEmpresas repositorioEmpresas;
    private final RegistroDeEventos registroDeEventos;
    private final ActivadorDeModulosDeEmpresa modulos;

    public FinalizarRegistroService(RepositorioEmpresas repositorioEmpresas,
                                    RegistroDeEventos registroDeEventos,
                                    ActivadorDeModulosDeEmpresa modulos) {
        this.repositorioEmpresas = repositorioEmpresas;
        this.registroDeEventos = registroDeEventos;
        this.modulos = modulos;
    }

    @Override
    public ResultadoFinalizarRegistro ejecutar(UUID empresaId) {
        Empresa empresa = repositorioEmpresas.buscarPorId(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        Set<String> seleccionados = modulos.codigosSeleccionados(empresaId);
        if (seleccionados.isEmpty()) {
            throw new IllegalArgumentException("Hay que elegir al menos un modulo antes de finalizar.");
        }

        empresa.finalizarRegistro(seleccionados);

        repositorioEmpresas.guardar(empresa);
        registroDeEventos.publicar(empresa.eventosPendientes());
        empresa.limpiarEventos();

        return new ResultadoFinalizarRegistro(
                empresa.getId(),
                empresa.getEstado().name().toLowerCase(),
                "https://" + empresa.getDominio());
    }
}
