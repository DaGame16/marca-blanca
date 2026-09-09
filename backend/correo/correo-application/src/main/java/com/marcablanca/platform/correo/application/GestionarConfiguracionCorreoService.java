package com.marcablanca.platform.correo.application;

import com.marcablanca.platform.correo.application.port.in.GestionarConfiguracionCorreo;
import com.marcablanca.platform.correo.application.port.out.RepositorioConfiguracionCorreo;
import com.marcablanca.platform.correo.domain.ConfiguracionCorreoNoEncontradaException;
import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;

import java.util.List;
import java.util.UUID;

public class GestionarConfiguracionCorreoService implements GestionarConfiguracionCorreo {

    private final RepositorioConfiguracionCorreo repositorio;

    public GestionarConfiguracionCorreoService(RepositorioConfiguracionCorreo repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public ConfiguracionSmtp crear(ComandoConfiguracionSmtp c) {
        return repositorio.crear(c.remitenteNombre(), c.remitenteCorreo(), c.responderA(), c.host(), c.puerto(),
                c.usuario(), c.secretoRef(), c.seguridad());
    }

    @Override
    public ConfiguracionSmtp actualizar(UUID id, ComandoConfiguracionSmtp c) {
        exigirQueExista(id);
        return repositorio.actualizar(id, c.remitenteNombre(), c.remitenteCorreo(), c.responderA(), c.host(),
                c.puerto(), c.usuario(), c.secretoRef(), c.seguridad());
    }

    @Override
    public ConfiguracionSmtp activar(UUID id) {
        exigirQueExista(id);
        // Orden importa: desactivar las demas ANTES de activar esta -- la tabla
        // tiene un indice unico parcial que no permite 2 activas al mismo tiempo.
        repositorio.desactivarTodasMenos(id);
        return repositorio.marcarActiva(id);
    }

    @Override
    public List<ConfiguracionSmtp> listar() {
        return repositorio.listarTodas();
    }

    @Override
    public ConfiguracionSmtp buscarPorId(UUID id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new ConfiguracionCorreoNoEncontradaException(id));
    }

    @Override
    public void eliminar(UUID id) {
        ConfiguracionSmtp existente = buscarPorId(id);
        if (existente.esActiva()) {
            throw new IllegalStateException(
                    "No se puede borrar la configuracion activa -- activa otra primero.");
        }
        repositorio.eliminar(id);
    }

    private void exigirQueExista(UUID id) {
        if (repositorio.buscarPorId(id).isEmpty()) {
            throw new ConfiguracionCorreoNoEncontradaException(id);
        }
    }
}
