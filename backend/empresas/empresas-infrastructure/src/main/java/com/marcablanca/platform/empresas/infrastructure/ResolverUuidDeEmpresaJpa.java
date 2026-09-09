package com.marcablanca.platform.empresas.infrastructure;

import com.marcablanca.platform.empresas.application.port.in.ResolverUuidDeEmpresa;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class ResolverUuidDeEmpresaJpa implements ResolverUuidDeEmpresa {

    private final EmpresaJpaRepository empresaJpaRepository;

    ResolverUuidDeEmpresaJpa(EmpresaJpaRepository empresaJpaRepository) {
        this.empresaJpaRepository = empresaJpaRepository;
    }

    @Override
    public UUID ejecutar(String identificadorEmpresa) {
        return empresaJpaRepository.findByIdentificadorAndEstado(identificadorEmpresa, "activa")
                .map(EmpresaEntity::getUuid)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontro una empresa activa con identificador: " + identificadorEmpresa));
    }
}
