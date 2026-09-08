package com.marcablanca.platform.omnicanal.application.port.in;

import java.time.OffsetDateTime;
import java.util.List;

public interface GestionarReprocesamiento {

    record ResultadoCaso(String casoId, boolean exito, String error) {
    }

    record ResultadoLote(int total, List<ResultadoCaso> detalles) {
    }

    ResultadoLote reprocesarPendientes();

    record ResultadoReproceso(int total, int exitos, int errores, List<String> casosConError) {
    }

    ResultadoReproceso reprocesarTodo(OffsetDateTime desde, OffsetDateTime hasta, boolean soloFaltantes);

    long contarParaReproceso(OffsetDateTime desde, OffsetDateTime hasta, boolean soloFaltantes);

    record EstadoReproceso(long total, long conAnalisisNuevo, long descartadosSinAnalisis, long faltantes,
                            long fallidos, boolean completo) {
    }

    EstadoReproceso estadoReproceso(OffsetDateTime desde, OffsetDateTime hasta);

    record ConteoPendientes(long total, List<String> muestraIds) {
    }

    ConteoPendientes contarPendientes(OffsetDateTime desde, OffsetDateTime hasta, boolean incluirIds);
}
