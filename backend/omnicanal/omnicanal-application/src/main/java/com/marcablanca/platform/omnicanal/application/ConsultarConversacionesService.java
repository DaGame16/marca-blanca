package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.ConsultarConversaciones;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.Pagina;
import com.marcablanca.platform.omnicanal.domain.Caso;
import com.marcablanca.platform.omnicanal.domain.Conversacion;
import com.marcablanca.platform.omnicanal.domain.ConversacionNoEncontradaException;

import java.time.OffsetDateTime;
import java.util.List;

public class ConsultarConversacionesService implements ConsultarConversaciones {

    private final RepositorioConversaciones repositorioConversaciones;
    private final RepositorioCasos repositorioCasos;
    private final RepositorioAnalisis repositorioAnalisis;

    public ConsultarConversacionesService(RepositorioConversaciones repositorioConversaciones,
                                          RepositorioCasos repositorioCasos,
                                          RepositorioAnalisis repositorioAnalisis) {
        this.repositorioConversaciones = repositorioConversaciones;
        this.repositorioCasos = repositorioCasos;
        this.repositorioAnalisis = repositorioAnalisis;
    }

    @Override
    public Pagina<Conversacion> listarRecientes(String idContacto, Integer pagina, Integer porPagina,
                                                 OffsetDateTime desde, OffsetDateTime hasta) {
        int p = pagina == null ? 1 : Math.max(1, pagina);
        int pp = porPagina == null ? 20 : Math.clamp(porPagina, 1, 100);
        return repositorioConversaciones.listarRecientes(idContacto, p, pp, desde, hasta);
    }

    @Override
    public ResumenContacto resumenDeContacto(String idContacto) {
        Conversacion conversacion = repositorioConversaciones.buscarPorIdContacto(idContacto)
                .orElseThrow(() -> new ConversacionNoEncontradaException(idContacto));

        List<CasoConAnalisis> casos = repositorioCasos.listarPorConversacion(conversacion.id()).stream()
                .map(this::conAnalisis)
                .toList();

        return new ResumenContacto(idContacto, conversacion.nombreContacto(), casos.size(), casos);
    }

    private CasoConAnalisis conAnalisis(Caso caso) {
        return new CasoConAnalisis(caso, repositorioAnalisis.buscarPorCasoId(caso.id()).orElse(null));
    }
}
