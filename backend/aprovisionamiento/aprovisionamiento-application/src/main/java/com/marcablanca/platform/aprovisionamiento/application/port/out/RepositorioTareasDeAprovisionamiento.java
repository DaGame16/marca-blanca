package com.marcablanca.platform.aprovisionamiento.application.port.out;

import com.marcablanca.platform.aprovisionamiento.domain.TareaDeAprovisionamiento;

import java.util.Optional;
import java.util.UUID;

/** Persistencia de la saga (tbl_aprovisionamiento_tareas). */
public interface RepositorioTareasDeAprovisionamiento {

    Optional<TareaDeAprovisionamiento> buscarPorEmpresa(UUID empresaId);

    void guardar(TareaDeAprovisionamiento tarea);
}