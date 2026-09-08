package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.ConsultarConversaciones;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.Pagina;
import com.marcablanca.platform.omnicanal.domain.Conversacion;
import com.marcablanca.platform.omnicanal.domain.ConversacionNoEncontradaException;

import java.time.OffsetDateTime;

public class ConsultarConversacionesService implements ConsultarConversaciones {

    private final RepositorioConversaciones repositorioConversaciones;

    public ConsultarConversacionesService(RepositorioConversaciones repositorioConversaciones) {
        this.repositorioConversaciones = repositorioConversaciones;
    }

    @Override
    public Pagina<Conversacion> listarRecientes(String idContacto, Integer pagina, Integer porPagina,
                                                 OffsetDateTime desde, OffsetDateTime hasta) {
        int p = pagina == null ? 1 : Math.max(1, pagina);
        int pp = porPagina == null ? 20 : Math.min(100, Math.max(1, porPagina));
        return repositorioConversaciones.listarRecientes(idContacto, p, pp, desde, hasta);
    }

    @Override
    public ResumenContacto resumenDeContacto(String idContacto) {
        Conversacion conversacion = repositorioConversaciones.buscarPorIdContacto(idContacto)
                .orElseThrow(() -> new ConversacionNoEncontradaException(idContacto));
        return new ResumenContacto(idContacto, conversacion.nombreContacto(), 0, java.util.List.of());
    }
}
