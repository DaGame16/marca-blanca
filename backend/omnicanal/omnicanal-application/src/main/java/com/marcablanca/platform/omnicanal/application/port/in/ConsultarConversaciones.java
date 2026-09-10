package com.marcablanca.platform.omnicanal.application.port.in;

import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.Pagina;
import com.marcablanca.platform.omnicanal.domain.AnalisisDeCaso;
import com.marcablanca.platform.omnicanal.domain.Caso;
import com.marcablanca.platform.omnicanal.domain.Conversacion;

import java.time.OffsetDateTime;
import java.util.List;

public interface ConsultarConversaciones {

    Pagina<Conversacion> listarRecientes(String idContacto, Integer pagina, Integer porPagina,
                                          OffsetDateTime desde, OffsetDateTime hasta);

    /** Un caso del contacto con su analisis IA, si ya existe (null si aun no se analizo). */
    record CasoConAnalisis(Caso caso, AnalisisDeCaso analisis) {
    }

    record ResumenContacto(String idContacto, String nombreContacto, int totalCasos, List<CasoConAnalisis> casos) {
    }

    ResumenContacto resumenDeContacto(String idContacto);
}
