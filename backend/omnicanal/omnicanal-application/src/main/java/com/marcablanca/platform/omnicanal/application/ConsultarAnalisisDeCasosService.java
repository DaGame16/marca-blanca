package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.ConsultarAnalisisDeCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.Pagina;
import com.marcablanca.platform.omnicanal.domain.AnalisisDeCaso;
import com.marcablanca.platform.omnicanal.domain.CasoNoEncontradoException;
import com.marcablanca.platform.omnicanal.domain.Turno;

import java.time.OffsetDateTime;
import java.util.List;

public class ConsultarAnalisisDeCasosService implements ConsultarAnalisisDeCasos {

    private final RepositorioAnalisis repositorioAnalisis;
    private final RepositorioConversaciones repositorioConversaciones;

    public ConsultarAnalisisDeCasosService(RepositorioAnalisis repositorioAnalisis,
                                            RepositorioConversaciones repositorioConversaciones) {
        this.repositorioAnalisis = repositorioAnalisis;
        this.repositorioConversaciones = repositorioConversaciones;
    }

    @Override
    public Pagina<AnalisisDeCaso> listar(Integer pagina, Integer porPagina, OffsetDateTime desde, OffsetDateTime hasta,
                                          String resultado, String motivoContacto, Boolean abandono,
                                          String abandonadoPor) {
        int p = pagina == null ? 1 : Math.max(1, pagina);
        int pp = porPagina == null ? 20 : Math.clamp(porPagina, 1, 100);
        return repositorioAnalisis.listar(p, pp, desde, hasta, resultado, motivoContacto, abandono, abandonadoPor);
    }

    @Override
    public DetalleAnalisis detalle(String idAnalisis) {
        AnalisisDeCaso analisis = repositorioAnalisis.buscarPorId(idAnalisis)
                .orElseThrow(() -> new CasoNoEncontradoException(idAnalisis));
        List<Turno> turnos = repositorioConversaciones.listarTurnos(analisis.casoId());
        return new DetalleAnalisis(analisis, turnos);
    }
}
