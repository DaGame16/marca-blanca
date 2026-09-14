package com.marcablanca.platform.omnicanal.application.port.in;

import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.Pagina;
import com.marcablanca.platform.omnicanal.domain.AnalisisDeCaso;
import com.marcablanca.platform.omnicanal.domain.Turno;

import java.time.OffsetDateTime;
import java.util.List;

public interface ConsultarAnalisisDeCasos {

    // java:S107: paginado + 4 filtros independientes -- mismo criterio que
    // RepositorioAnalisis.listar (port out), que este metodo delega.
    Pagina<AnalisisDeCaso> listar(Integer pagina, Integer porPagina, OffsetDateTime desde, OffsetDateTime hasta, //NOSONAR ver comentario arriba
                                   String resultado, String motivoContacto, Boolean abandono, String abandonadoPor);

    record DetalleAnalisis(AnalisisDeCaso analisis, List<Turno> turnosDelCaso) {
    }

    DetalleAnalisis detalle(String idAnalisis);
}
