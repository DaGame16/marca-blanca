package com.marcablanca.platform.omnicanal.application.port.in;

import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.Pagina;
import com.marcablanca.platform.omnicanal.domain.Caso;
import com.marcablanca.platform.omnicanal.domain.Conversacion;

import java.time.OffsetDateTime;
import java.util.List;

public interface ConsultarConversaciones {

    Pagina<Conversacion> listarRecientes(String idContacto, Integer pagina, Integer porPagina,
                                          OffsetDateTime desde, OffsetDateTime hasta);

    record ResumenContacto(String idContacto, String nombreContacto, int totalCasos, List<Caso> casos) {
    }

    ResumenContacto resumenDeContacto(String idContacto);
}
