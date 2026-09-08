package com.marcablanca.platform.omnicanal.application.port.out;

import java.util.Optional;

/** Adaptador de salida hacia la API externa de LIWA (chat.liwa.co) -- solo para el backfill de ads. */
public interface ClienteLiwa {
    Optional<Boolean> consultarSiVieneDeAds(String idContacto, String apiToken);
}
