package com.marcablanca.platform.empresas.infrastructure;

import com.marcablanca.platform.empresas.application.port.in.ObtenerNombreDeEmpresa;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class ObtenerNombreDeEmpresaJpa implements ObtenerNombreDeEmpresa {

    private final EmpresaJpaRepository empresaJpaRepository;

    ObtenerNombreDeEmpresaJpa(EmpresaJpaRepository empresaJpaRepository) {
        this.empresaJpaRepository = empresaJpaRepository;
    }

    @Override
    public Optional<String> ejecutar(String identificadorEmpresa) {
        return empresaJpaRepository.findByIdentificadorAndEstado(identificadorEmpresa, "activa")
                .map(EmpresaEntity::getNombreLegal);
    }
}
