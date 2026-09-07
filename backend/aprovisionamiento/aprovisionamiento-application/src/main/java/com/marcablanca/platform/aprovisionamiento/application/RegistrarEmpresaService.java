package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.RegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.CifradorDeContrasenaMaestra;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RegistroDeEventos;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaYaExisteException;
import com.marcablanca.platform.aprovisionamiento.domain.HashContrasenaMaestra;
import com.marcablanca.platform.aprovisionamiento.domain.Identificador;

import java.util.UUID;

/**
 * Capa 1. Orquesta el alta: valida unicidad, cifra la contrasena maestra,
 * crea el agregado y lo persiste JUNTO con el evento de outbox.
 *
 * La atomicidad (guardar empresa + evento en una transaccion) la garantiza el
 * decorador @Transactional de infraestructura que envuelve este servicio.
 */
public class RegistrarEmpresaService implements RegistrarEmpresa {

    private final RepositorioEmpresas repositorioEmpresas;
    private final RegistroDeEventos registroDeEventos;
    private final CifradorDeContrasenaMaestra cifrador;

    public RegistrarEmpresaService(RepositorioEmpresas repositorioEmpresas,
                                   RegistroDeEventos registroDeEventos,
                                   CifradorDeContrasenaMaestra cifrador) {
        this.repositorioEmpresas = repositorioEmpresas;
        this.registroDeEventos = registroDeEventos;
        this.cifrador = cifrador;
    }

    @Override
    public UUID ejecutar(ComandoRegistrarEmpresa comando) {
        Identificador identificador = new Identificador(comando.identificador());

        if (repositorioEmpresas.existePorIdentificador(identificador)) {
            throw new EmpresaYaExisteException("el identificador '" + identificador.valor() + "'");
        }
        String dominio = normalizar(comando.dominio());
        if (dominio != null && repositorioEmpresas.existePorDominio(dominio)) {
            throw new EmpresaYaExisteException("el dominio '" + dominio + "'");
        }

        HashContrasenaMaestra hash = cifrador.cifrar(comando.contrasenaMaestra());

        Empresa empresa = Empresa.registrar(
                identificador,
                comando.nombreLegal(),
                comando.nombreComercial(),
                dominio,
                hash,
                comando.modulosSolicitados());

        repositorioEmpresas.guardar(empresa);
        registroDeEventos.publicar(empresa.eventosPendientes());
        empresa.limpiarEventos();

        return empresa.getId();
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.isBlank() ? null : limpio;
    }
}