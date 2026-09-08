package com.marcablanca.platform.aprovisionamiento.application.port.out;

import com.marcablanca.platform.aprovisionamiento.domain.Personalizacion;

import java.util.UUID;

/** Persiste la personalizacion visual sobre plataforma.tbl_empresas_marca (upsert 1:1 con la empresa). */
public interface RepositorioPersonalizacion {
    void guardar(UUID empresaId, Personalizacion personalizacion);
}
