package com.marcablanca.platform.modulosempresa.application;

import com.marcablanca.platform.modulosempresa.application.port.in.GestionarCatalogoDeModulos;
import com.marcablanca.platform.modulosempresa.application.port.out.RepositorioModulos;
import com.marcablanca.platform.modulosempresa.domain.Modulo;
import com.marcablanca.platform.modulosempresa.domain.ModuloNoEncontradoException;
import com.marcablanca.platform.modulosempresa.domain.ModuloYaExisteException;

import java.util.UUID;

public class GestionarCatalogoDeModulosService implements GestionarCatalogoDeModulos {

    private final RepositorioModulos repositorioModulos;

    public GestionarCatalogoDeModulosService(RepositorioModulos repositorioModulos) {
        this.repositorioModulos = repositorioModulos;
    }

    @Override
    public Modulo crear(ComandoModulo comando) {
        Modulo modulo = Modulo.nuevo(
                comando.codigo(), comando.nombre(), comando.descripcion(), comando.precio(), comando.moneda());
        if (repositorioModulos.existePorCodigo(modulo.codigo())) {
            throw new ModuloYaExisteException(modulo.codigo());
        }
        return repositorioModulos.guardar(modulo);
    }

    @Override
    public Modulo actualizar(UUID id, ComandoModulo comando) {
        Modulo actual = repositorioModulos.buscarPorId(id)
                .orElseThrow(() -> new ModuloNoEncontradoException(id.toString()));

        Modulo cambiado = new Modulo(
                actual.id(), actual.codigo(),   // el codigo no se cambia (identifica el modulo en tbl_empresa_modulos)
                comando.nombre(), comando.descripcion(), comando.precio(), comando.moneda());
        return repositorioModulos.guardar(cambiado);
    }

    @Override
    public void eliminar(UUID id) {
        repositorioModulos.buscarPorId(id)
                .orElseThrow(() -> new ModuloNoEncontradoException(id.toString()));
        repositorioModulos.eliminar(id);
    }
}
