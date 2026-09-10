package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.EjecutarBackfillDeAds;
import com.marcablanca.platform.omnicanal.application.port.out.ClienteLiwa;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.RefContacto;
import com.marcablanca.platform.omnicanal.domain.Caso;

import java.util.List;

/**
 * Por cada contacto de la empresa activa consulta a LIWA si vino de ads y
 * propaga la marca: a la conversacion (siempre), y si vino de ads, al caso
 * mas antiguo (el mas probable de corresponder al momento en que llego por
 * ads) y al analisis de ese caso. Mismo criterio que el liwa-webhook original.
 *
 * Un contacto sin dato (sin token de LIWA, error de la API) se cuenta y se
 * sigue -- no aborta el backfill.
 */
public class EjecutarBackfillDeAdsService implements EjecutarBackfillDeAds {

    private final RepositorioConversaciones repositorioConversaciones;
    private final RepositorioCasos repositorioCasos;
    private final RepositorioAnalisis repositorioAnalisis;
    private final ClienteLiwa clienteLiwa;

    public EjecutarBackfillDeAdsService(RepositorioConversaciones repositorioConversaciones,
                                       RepositorioCasos repositorioCasos,
                                       RepositorioAnalisis repositorioAnalisis,
                                       ClienteLiwa clienteLiwa) {
        this.repositorioConversaciones = repositorioConversaciones;
        this.repositorioCasos = repositorioCasos;
        this.repositorioAnalisis = repositorioAnalisis;
        this.clienteLiwa = clienteLiwa;
    }

    @Override
    public ResultadoBackfill ejecutar() {
        List<RefContacto> contactos = repositorioConversaciones.contactos();
        int conAds = 0;
        int sinDato = 0;

        for (RefContacto contacto : contactos) {
            var respuesta = clienteLiwa.consultarSiVieneDeAds(contacto.idContacto());
            if (respuesta.isEmpty()) {
                sinDato++;
                continue;
            }

            boolean vieneDeAds = respuesta.get();
            repositorioConversaciones.marcarVieneDeAds(contacto.conversacionId(), vieneDeAds);

            if (vieneDeAds) {
                conAds++;
                casoMasAntiguo(contacto.conversacionId()).ifPresent(caso -> {
                    repositorioCasos.marcarEsDeAds(caso.id(), true);
                    repositorioAnalisis.marcarEsDeAdsPorCaso(caso.id(), true);
                });
            }
        }

        return new ResultadoBackfill(contactos.size(), conAds, sinDato);
    }

    /** listarPorConversacion viene del mas reciente al mas viejo -> el ultimo es el mas antiguo. */
    private java.util.Optional<Caso> casoMasAntiguo(Long conversacionId) {
        List<Caso> casos = repositorioCasos.listarPorConversacion(conversacionId);
        return casos.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(casos.get(casos.size() - 1));
    }
}
