package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.domain.PerfilDeAnalisisOmnicanal;
import com.marcablanca.platform.omnicanal.infrastructure.CifradorOmnicanal;
import com.marcablanca.platform.omnicanal.infrastructure.PerfilDeAnalisisPredeterminado;
import org.springframework.stereotype.Component;

@Component
class RepositorioConfiguracionOmnicanalJpa implements RepositorioConfiguracionOmnicanal {

    private final ConfiguracionOmnicanalJpaRepository repo;
    private final CifradorOmnicanal cifrador;
    private final ObjectMapper mapper;

    RepositorioConfiguracionOmnicanalJpa(ConfiguracionOmnicanalJpaRepository repo, CifradorOmnicanal cifrador,
                                         ObjectMapper mapper) {
        this.repo = repo;
        this.cifrador = cifrador;
        this.mapper = mapper;
    }

    @Override
    public ConfiguracionDeTenant deLaEmpresaActiva() {
        var entidad = repo.findFirstByOrderByIdAsc().orElse(null);
        if (entidad == null) {
            return new ConfiguracionDeTenant(PerfilDeAnalisisPredeterminado.ISP, null, false, null);
        }
        return new ConfiguracionDeTenant(
                perfilDesde(entidad.getPerfilAnalisis()),
                cifrador.descifrar(entidad.getLiwaApiToken()),
                entidad.isIaHabilitada(),
                entidad.getOpenaiModelo());
    }

    /**
     * El JSON de la columna es un override PARCIAL: cada campo ausente cae al
     * perfil por defecto. JSON invalido o vacio => perfil por defecto entero
     * (no se tumba la ingesta por una config mal escrita).
     */
    private PerfilDeAnalisisOmnicanal perfilDesde(String json) {
        PerfilDeAnalisisOmnicanal base = PerfilDeAnalisisPredeterminado.ISP;
        if (json == null || json.isBlank()) {
            return base;
        }
        try {
            JsonNode n = mapper.readTree(json);
            return new PerfilDeAnalisisOmnicanal(
                    texto(n, "nombreEmpresa", base.nombreEmpresa()),
                    texto(n, "promptSistema", base.promptSistema()),
                    texto(n, "plantillaPrompt", base.plantillaPrompt()),
                    texto(n, "liwaBaseUrl", base.liwaBaseUrl()),
                    texto(n, "liwaCustomFieldAds", base.liwaCustomFieldAds()));
        } catch (Exception e) {
            return base;
        }
    }

    private static String texto(JsonNode n, String campo, String porDefecto) {
        JsonNode v = n.get(campo);
        return (v == null || v.isNull() || v.asText().isBlank()) ? porDefecto : v.asText();
    }
}
