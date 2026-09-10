package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.ConfigurarOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RegistroRuteoOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal.ConfiguracionDeTenant;

public class ConfigurarOmnicanalService implements ConfigurarOmnicanal {

    private static final String RUTA_WEBHOOK = "/api/v1/omnicanal/webhook/chat-history";

    private final RepositorioConfiguracionOmnicanal configuracion;
    private final RegistroRuteoOmnicanal ruteo;
    private final String webhookUrlBase;

    public ConfigurarOmnicanalService(RepositorioConfiguracionOmnicanal configuracion, RegistroRuteoOmnicanal ruteo,
                                      String webhookUrlBase) {
        this.configuracion = configuracion;
        this.ruteo = ruteo;
        this.webhookUrlBase = webhookUrlBase;
    }

    @Override
    public VistaConfig ver() {
        return armarVista(ruteo.secretoWebhook());
    }

    @Override
    public void actualizarAjustes(boolean iaHabilitada, String openaiModelo, String liwaBaseUrl,
                                  String liwaCustomFieldAds) {
        configuracion.guardarAjustes(iaHabilitada, vacioANull(openaiModelo), vacioANull(liwaBaseUrl),
                vacioANull(liwaCustomFieldAds));
    }

    @Override
    public void definirLiwaToken(String tokenPlano) {
        configuracion.guardarLiwaToken(vacioANull(tokenPlano));
    }

    @Override
    public void borrarLiwaToken() {
        configuracion.guardarLiwaToken(null);
    }

    @Override
    public VistaConfig rotarSecreto() {
        return armarVista(ruteo.rotarSecretoWebhook());
    }

    private VistaConfig armarVista(String secreto) {
        ConfiguracionDeTenant cfg = configuracion.deLaEmpresaActiva();
        return new VistaConfig(
                webhookUrlBase + RUTA_WEBHOOK,
                secreto,
                cfg.iaHabilitada(),
                cfg.openaiModelo(),
                cfg.perfil().liwaBaseUrl(),
                cfg.perfil().liwaCustomFieldAds(),
                cfg.liwaApiToken() != null && !cfg.liwaApiToken().isBlank());
    }

    private static String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.strip();
    }
}
