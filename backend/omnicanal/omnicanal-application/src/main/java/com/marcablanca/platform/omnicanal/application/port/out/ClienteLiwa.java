package com.marcablanca.platform.omnicanal.application.port.out;

import java.util.Optional;

/**
 * Adaptador de salida hacia la API externa de LIWA -- solo para el backfill de
 * atribucion de ads. El adaptador resuelve base, token y custom field desde la
 * configuracion de la empresa activa (RepositorioConfiguracionOmnicanal).
 */
public interface ClienteLiwa {
    Optional<Boolean> consultarSiVieneDeAds(String idContacto);
}
