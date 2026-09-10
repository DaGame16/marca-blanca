package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.domain.PerfilDeAnalisisOmnicanal;
import com.marcablanca.platform.omnicanal.infrastructure.CifradorOmnicanal;
import com.marcablanca.platform.omnicanal.infrastructure.PerfilDeAnalisisPredeterminado;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

    @Override
    public void guardarAjustes(boolean iaHabilitada, String openaiModelo, String liwaBaseUrl,
                               String liwaCustomFieldAds) {
        ConfiguracionOmnicanalEntity e = repo.findFirstByOrderByIdAsc()
                .orElseGet(ConfiguracionOmnicanalEntity::nueva);
        e.aplicarAjustes(iaHabilitada, openaiModelo, liwaBaseUrl, liwaCustomFieldAds);
        repo.save(e);
    }

    @Override
    public void guardarLiwaToken(String tokenPlano) {
        ConfiguracionOmnicanalEntity e = repo.findFirstByOrderByIdAsc()
                .orElseGet(ConfiguracionOmnicanalEntity::nueva);
        e.aplicarLiwaTokenCifrado(
                (tokenPlano == null || tokenPlano.isBlank()) ? null : cifrador.cifrar(tokenPlano));
        repo.save(e);
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
                    texto(n, "liwaCustomFieldAds", base.liwaCustomFieldAds()),
                    lista(n, "opcionesMenu", base.opcionesMenu()),
                    lista(n, "marcadoresEncuesta", base.marcadoresEncuesta()),
                    lista(n, "frasesMarcaRuido", base.frasesMarcaRuido()),
                    lista(n, "lugaresConocidos", base.lugaresConocidos()),
                    mapa(n, "abreviaturasLugar", base.abreviaturasLugar()),
                    lista(n, "lugaresVacios", base.lugaresVacios()));
        } catch (Exception e) {
            return base;
        }
    }

    private static String texto(JsonNode n, String campo, String porDefecto) {
        JsonNode v = n.get(campo);
        return (v == null || v.isNull() || v.asString().isBlank()) ? porDefecto : v.asString();
    }

    private List<String> lista(JsonNode n, String campo, List<String> porDefecto) {
        JsonNode v = n.get(campo);
        if (v == null || v.isNull() || !v.isArray()) {
            return porDefecto;
        }
        return mapper.convertValue(v, new TypeReference<List<String>>() {
        });
    }

    private Map<String, String> mapa(JsonNode n, String campo, Map<String, String> porDefecto) {
        JsonNode v = n.get(campo);
        if (v == null || v.isNull() || !v.isObject()) {
            return porDefecto;
        }
        return mapper.convertValue(v, new TypeReference<Map<String, String>>() {
        });
    }
}
