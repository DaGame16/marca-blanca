package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioPersonalizacion;
import com.marcablanca.platform.aprovisionamiento.domain.Personalizacion;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class RepositorioPersonalizacionJpaAdapter implements RepositorioPersonalizacion {

    private final SpringDataMarcaRepository marcaRepo;
    private final SpringDataEmpresaRepository empresaRepo;

    RepositorioPersonalizacionJpaAdapter(SpringDataMarcaRepository marcaRepo,
                                         SpringDataEmpresaRepository empresaRepo) {
        this.marcaRepo = marcaRepo;
        this.empresaRepo = empresaRepo;
    }

    @Override
    public void guardar(UUID empresaId, Personalizacion p) {
        Long empresaIdInterno = empresaRepo.buscarIdInternoPorUuid(empresaId)
                .orElseThrow(() -> new IllegalStateException("No existe la empresa " + empresaId + "."));

        MarcaDeAprovisionamientoEntity e = marcaRepo.findByEmpresaId(empresaIdInterno)
                .orElseGet(MarcaDeAprovisionamientoEntity::new);
        e.setEmpresaId(empresaIdInterno);
        e.setColorPrimario(p.colorPrimario() == null ? null : p.colorPrimario().valor());
        e.setColorSecundario(p.colorSecundario() == null ? null : p.colorSecundario().valor());
        e.setUrlLogo(p.urlLogo());
        e.setTipoLogin((short) p.tipoLogin());
        e.setTipoPantallaPrincipal((short) p.tipoPantallaPrincipal());
        marcaRepo.save(e);
    }
}
