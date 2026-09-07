package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioTareasDeAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.domain.TareaDeAprovisionamiento;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class RepositorioTareasDeAprovisionamientoJpaAdapter implements RepositorioTareasDeAprovisionamiento {

    private final SpringDataTareaDeAprovisionamientoRepository tareaRepo;
    private final SpringDataEmpresaRepository empresaRepo;

    RepositorioTareasDeAprovisionamientoJpaAdapter(
            SpringDataTareaDeAprovisionamientoRepository tareaRepo,
            SpringDataEmpresaRepository empresaRepo) {
        this.tareaRepo = tareaRepo;
        this.empresaRepo = empresaRepo;
    }

    @Override
    public Optional<TareaDeAprovisionamiento> buscarPorEmpresa(UUID empresaId) {
        return empresaRepo.buscarIdInternoPorUuid(empresaId)
                .flatMap(tareaRepo::findByEmpresaId)
                .map(e -> TareaDeAprovisionamientoMapper.aDominio(e, empresaId));
    }

    @Override
    public void guardar(TareaDeAprovisionamiento tarea) {
        Long empresaIdInterno = empresaRepo.buscarIdInternoPorUuid(tarea.getEmpresaId())
                .orElseThrow(() -> new IllegalStateException(
                        "No existe la empresa " + tarea.getEmpresaId() + " para su tarea de aprovisionamiento."));

        TareaDeAprovisionamientoEntity entidad = tareaRepo.findByUuid(tarea.getId())
                .orElseGet(TareaDeAprovisionamientoEntity::new);
        TareaDeAprovisionamientoMapper.aplicar(tarea, empresaIdInterno, entidad);
        tareaRepo.save(entidad);
    }
}