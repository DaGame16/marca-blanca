package com.marcablanca.platform.empresas.infrastructure;

import com.marcablanca.platform.empresas.application.port.out.RepositorioEmpresaConexiones;
import com.marcablanca.platform.empresas.domain.EmpresaConexion;
import com.marcablanca.platform.empresas.domain.EmpresaConexionDeEmpresa;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class RepositorioEmpresaConexionesJpa implements RepositorioEmpresaConexiones {

    private final EmpresaConexionJpaRepository empresaConexionJpaRepository;

    RepositorioEmpresaConexionesJpa(EmpresaConexionJpaRepository empresaConexionJpaRepository) {
        this.empresaConexionJpaRepository = empresaConexionJpaRepository;
    }

    @Override
    public Optional<EmpresaConexion> buscarConexionActivaPorIdentificador(String identificadorEmpresa) {
        return empresaConexionJpaRepository
                .buscarConexionActivaPorIdentificador(identificadorEmpresa)
                .map(entity -> new EmpresaConexion(entity.getHost(), entity.getPuerto(), entity.getNombreBd()));
    }

    @Override
    public List<EmpresaConexionDeEmpresa> listarConexionesActivas() {
        return empresaConexionJpaRepository.listarConexionesActivas().stream()
                .map(fila -> new EmpresaConexionDeEmpresa(
                        fila.getIdentificador(),
                        new EmpresaConexion(fila.getHost(), fila.getPuerto(), fila.getNombreBd())))
                .toList();
    }
}
