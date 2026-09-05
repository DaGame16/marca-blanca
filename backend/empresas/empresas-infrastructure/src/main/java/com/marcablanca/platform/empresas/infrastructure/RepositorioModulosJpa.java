package com.marcablanca.platform.empresas.infrastructure;

import com.marcablanca.platform.empresas.application.port.out.RepositorioModulos;
import com.marcablanca.platform.empresas.domain.Modulo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class RepositorioModulosJpa implements RepositorioModulos {

    private final ModuloJpaRepository moduloJpaRepository;

    RepositorioModulosJpa(ModuloJpaRepository moduloJpaRepository) {
        this.moduloJpaRepository = moduloJpaRepository;
    }

    @Override
    public List<Modulo> listarTodos() {
        return moduloJpaRepository.findAll().stream()
                .map(m -> new Modulo(m.getUuid(), m.getCodigo(), m.getNombre(), m.getDescripcion()))
                .toList();
    }
}
