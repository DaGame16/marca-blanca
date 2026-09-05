package com.marcablanca.platform.modulosempresa.infrastructure;

import com.marcablanca.platform.modulosempresa.application.port.out.RepositorioModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.domain.EmpresaNoEncontradaException;
import com.marcablanca.platform.modulosempresa.domain.ModuloDeEmpresa;
import com.marcablanca.platform.modulosempresa.domain.ModuloNoEncontradoException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
class RepositorioModulosDeEmpresaJpa implements RepositorioModulosDeEmpresa {

    private final ModuloJpaRepository moduloJpaRepository;
    private final EmpresaModuloJpaRepository empresaModuloJpaRepository;

    RepositorioModulosDeEmpresaJpa(ModuloJpaRepository moduloJpaRepository,
                                    EmpresaModuloJpaRepository empresaModuloJpaRepository) {
        this.moduloJpaRepository = moduloJpaRepository;
        this.empresaModuloJpaRepository = empresaModuloJpaRepository;
    }

    @Override
    public List<ModuloDeEmpresa> listarPorEmpresa(UUID empresaId) {
        Long empresaIdInterno = empresaModuloJpaRepository.buscarEmpresaIdInternoPorUuid(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));

        Set<String> codigosActivos = new HashSet<>(
                empresaModuloJpaRepository.listarCodigosActivosDeEmpresa(empresaIdInterno));

        return moduloJpaRepository.findAll().stream()
                .map(m -> new ModuloDeEmpresa(m.getCodigo(), m.getNombre(), m.getDescripcion(),
                        codigosActivos.contains(m.getCodigo())))
                .toList();
    }

    @Override
    public void activar(UUID empresaId, String codigoModulo) {
        Long empresaIdInterno = resolverEmpresaIdInterno(empresaId);
        Long moduloIdInterno = resolverModuloIdInterno(codigoModulo);

        empresaModuloJpaRepository.findByEmpresaIdAndModuloId(empresaIdInterno, moduloIdInterno)
                .ifPresentOrElse(
                        existente -> {
                            existente.activar();
                            empresaModuloJpaRepository.save(existente);
                        },
                        () -> empresaModuloJpaRepository.save(new EmpresaModuloEntity(
                                UUID.randomUUID(), empresaIdInterno, moduloIdInterno, true, OffsetDateTime.now()))
                );
    }

    @Override
    public void desactivar(UUID empresaId, String codigoModulo) {
        Long empresaIdInterno = resolverEmpresaIdInterno(empresaId);
        Long moduloIdInterno = resolverModuloIdInterno(codigoModulo);

        // Si no existia el registro, no hay nada que desactivar -- idempotente, no es error.
        empresaModuloJpaRepository.findByEmpresaIdAndModuloId(empresaIdInterno, moduloIdInterno)
                .ifPresent(existente -> {
                    existente.desactivar();
                    empresaModuloJpaRepository.save(existente);
                });
    }

    private Long resolverEmpresaIdInterno(UUID empresaId) {
        return empresaModuloJpaRepository.buscarEmpresaIdInternoPorUuid(empresaId)
                .orElseThrow(() -> new EmpresaNoEncontradaException(empresaId));
    }

    private Long resolverModuloIdInterno(String codigoModulo) {
        return moduloJpaRepository.findByCodigo(codigoModulo)
                .map(ModuloEntity::getId)
                .orElseThrow(() -> new ModuloNoEncontradoException(codigoModulo));
    }
}
