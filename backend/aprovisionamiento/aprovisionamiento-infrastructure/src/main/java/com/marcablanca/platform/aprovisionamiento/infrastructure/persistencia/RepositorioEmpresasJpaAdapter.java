package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.Identificador;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class RepositorioEmpresasJpaAdapter implements RepositorioEmpresas {

    private final SpringDataEmpresaRepository repo;

    RepositorioEmpresasJpaAdapter(SpringDataEmpresaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void guardar(Empresa empresa) {
        EmpresaDeAprovisionamientoEntity entidad = repo.findByUuid(empresa.getId())
                .orElseGet(EmpresaDeAprovisionamientoEntity::new);
        EmpresaMapper.aplicar(empresa, entidad);
        repo.save(entidad);
    }

    @Override
    public boolean existePorIdentificador(Identificador identificador) {
        return repo.existsByIdentificador(identificador.valor());
    }

    @Override
    public boolean existePorDominio(String dominio) {
        return repo.existsByDominio(dominio);
    }

    @Override
    public Optional<Empresa> buscarPorId(UUID id) {
        return repo.findByUuid(id).map(EmpresaMapper::aDominio);
    }
}