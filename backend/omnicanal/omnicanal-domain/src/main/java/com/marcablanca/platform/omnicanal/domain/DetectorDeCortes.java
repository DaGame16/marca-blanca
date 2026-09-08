package com.marcablanca.platform.omnicanal.domain;

import java.util.ArrayList;
import java.util.List;

/** Segmenta una lista de turnos en "casos" por brecha de tiempo (+24h = otro caso). */
public final class DetectorDeCortes {

    private static final long UMBRAL_HORAS = 24;

    private DetectorDeCortes() {
    }

    public static List<Integer> detectarCortes(List<TurnoParseado> turnos) {
        List<Integer> cortes = new ArrayList<>();
        cortes.add(0);

        for (int i = 1; i < turnos.size(); i++) {
            var anterior = turnos.get(i - 1).fecha();
            var actual = turnos.get(i).fecha();
            if (anterior == null || actual == null) {
                continue;
            }
            long diffHoras = java.time.Duration.between(anterior, actual).toHours();
            if (diffHoras > UMBRAL_HORAS) {
                cortes.add(i);
            }
        }
        return cortes;
    }
}
