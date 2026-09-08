package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.in.GestionarReprocesamiento;
import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.domain.Caso;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class GestionarReprocesamientoService implements GestionarReprocesamiento {

    private final RepositorioCasos repositorioCasos;
    private final RepositorioConversaciones repositorioConversaciones;
    private final AnalizadorDeConversacion analizadorDeConversacion;
    private final RepositorioAnalisisEscritor escritorAnalisis;

    public GestionarReprocesamientoService(RepositorioCasos repositorioCasos,
                                            RepositorioConversaciones repositorioConversaciones,
                                            AnalizadorDeConversacion analizadorDeConversacion,
                                            RepositorioAnalisisEscritor escritorAnalisis) {
        this.repositorioCasos = repositorioCasos;
        this.repositorioConversaciones = repositorioConversaciones;
        this.analizadorDeConversacion = analizadorDeConversacion;
        this.escritorAnalisis = escritorAnalisis;
    }

    @Override
    public ResultadoLote reprocesarPendientes() {
        List<Caso> pendientes = repositorioCasos.listarPendientes(null, null);
        List<ResultadoCaso> resultados = new ArrayList<>();
        for (Caso caso : pendientes) {
            try {
                escritorAnalisis.analizarYGuardar(caso, analizadorDeConversacion, repositorioConversaciones,
                        repositorioCasos, null);
                resultados.add(new ResultadoCaso(caso.uuid().toString(), true, null));
            } catch (Exception e) {
                resultados.add(new ResultadoCaso(caso.uuid().toString(), false, e.getMessage()));
            }
        }
        return new ResultadoLote(pendientes.size(), resultados);
    }

    @Override
    public ResultadoReproceso reprocesarTodo(OffsetDateTime desde, OffsetDateTime hasta, boolean soloFaltantes) {
        List<Caso> casos = soloFaltantes
                ? repositorioCasos.listarSinReanalizar(desde, hasta)
                : repositorioCasos.listarPendientes(desde, hasta);
        int exitos = 0;
        List<String> conError = new ArrayList<>();
        for (Caso caso : casos) {
            try {
                escritorAnalisis.analizarYGuardar(caso, analizadorDeConversacion, repositorioConversaciones,
                        repositorioCasos, null);
                exitos++;
            } catch (Exception e) {
                conError.add(caso.uuid().toString());
                repositorioCasos.marcarProcesada(caso.id(), false);
            }
        }
        return new ResultadoReproceso(casos.size(), exitos, conError.size(), conError);
    }

    @Override
    public long contarParaReproceso(OffsetDateTime desde, OffsetDateTime hasta, boolean soloFaltantes) {
        return soloFaltantes
                ? repositorioCasos.contarSinReanalizar(desde, hasta)
                : repositorioCasos.contarPendientes(desde, hasta);
    }

    @Override
    public EstadoReproceso estadoReproceso(OffsetDateTime desde, OffsetDateTime hasta) {
        long total = repositorioCasos.contarTotal(desde, hasta);
        long faltantes = repositorioCasos.contarSinReanalizar(desde, hasta);
        long fallidos = repositorioCasos.contarPendientes(desde, hasta);
        long conAnalisisNuevo = repositorioCasos.contarConAnalisisNuevo(desde, hasta);
        return new EstadoReproceso(total, conAnalisisNuevo, total - conAnalisisNuevo - faltantes, faltantes,
                fallidos, faltantes == 0);
    }

    @Override
    public ConteoPendientes contarPendientes(OffsetDateTime desde, OffsetDateTime hasta, boolean incluirIds) {
        long total = repositorioCasos.contarPendientes(desde, hasta);
        if (!incluirIds) {
            return new ConteoPendientes(total, List.of());
        }
        List<String> ids = repositorioCasos.listarPendientes(desde, hasta).stream()
                .limit(200).map(c -> c.uuid().toString()).toList();
        return new ConteoPendientes(total, ids);
    }
}
