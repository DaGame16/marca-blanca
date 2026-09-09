package com.marcablanca.platform.modulosempresa.infrastructure;

import com.marcablanca.platform.modulosempresa.application.port.out.RepositorioModulos;
import com.marcablanca.platform.modulosempresa.domain.Modulo;
import com.marcablanca.platform.modulosempresa.domain.ModuloEnUsoException;
import com.marcablanca.platform.modulosempresa.domain.ModuloNoEncontradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class RepositorioModulosJpa implements RepositorioModulos {

    private final ModuloJpaRepository moduloJpaRepository;

    RepositorioModulosJpa(ModuloJpaRepository moduloJpaRepository) {
        this.moduloJpaRepository = moduloJpaRepository;
    }

    @Override
    public List<Modulo> listarTodos() {
        return moduloJpaRepository.findAll().stream().map(RepositorioModulosJpa::aDominio).toList();
    }

    @Override
    public Optional<Modulo> buscarPorId(UUID id) {
        return moduloJpaRepository.findByUuid(id).map(RepositorioModulosJpa::aDominio);
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return moduloJpaRepository.existsByCodigo(codigo);
    }

    @Override
    public Modulo guardar(Modulo modulo) {
        ModuloEntity entidad = moduloJpaRepository.findByUuid(modulo.id()).orElseGet(ModuloEntity::new);
        entidad.setUuid(modulo.id());
        entidad.setCodigo(modulo.codigo());
        entidad.setNombre(modulo.nombre());
        entidad.setDescripcion(modulo.descripcion());
        entidad.setPrecio(modulo.precio());
        entidad.setMoneda(modulo.moneda());
        return aDominio(moduloJpaRepository.save(entidad));
    }

    @Override
    public void eliminar(UUID id) {
        ModuloEntity entidad = moduloJpaRepository.findByUuid(id)
                .orElseThrow(() -> new ModuloNoEncontradoException(id.toString()));
        try {
            moduloJpaRepository.delete(entidad);
            moduloJpaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ModuloEnUsoException(entidad.getCodigo());
        }
    }

    private static Modulo aDominio(ModuloEntity m) {
        return new Modulo(m.getUuid(), m.getCodigo(), m.getNombre(), m.getDescripcion(), m.getPrecio(), m.getMoneda());
    }
}
