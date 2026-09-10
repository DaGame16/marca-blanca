package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal.ConfiguracionDeTenant;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.domain.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Pieza compartida entre ingesta y reprocesamiento: dado un Caso, arma los
 * turnos relevantes (con el vocabulario de la empresa activa), llama al puerto
 * de IA, calcula abandono, normaliza el municipio y guarda. Se separo para no
 * duplicarla entre los 2 servicios que la usan.
 */
public class RepositorioAnalisisEscritor {

    private static final String ULTIMO_CLIENTE = "cliente";
    private static final String ULTIMO_ASESOR_O_BOT = "asesor_o_bot";
    private static final long MINUTOS_ESPERA_UMBRAL = 30;

    private final RepositorioAnalisis repositorioAnalisis;
    private final RepositorioConfiguracionOmnicanal configuracion;

    public RepositorioAnalisisEscritor(RepositorioAnalisis repositorioAnalisis,
                                       RepositorioConfiguracionOmnicanal configuracion) {
        this.repositorioAnalisis = repositorioAnalisis;
        this.configuracion = configuracion;
    }

    public void analizarYGuardar(Caso caso, AnalizadorDeConversacion analizador,
                                  RepositorioConversaciones repoConversaciones, RepositorioCasos repoCasos,
                                  String idContactoConocido) {
        ConfiguracionDeTenant cfg = configuracion.deLaEmpresaActiva();
        NormalizadorDeMunicipio normalizadorMunicipio = new NormalizadorDeMunicipio(cfg.perfil());

        List<TurnoParseado> relevantes = relevantesDelCaso(caso, repoConversaciones, cfg).orElse(null);
        if (relevantes == null) {
            repositorioAnalisis.eliminarPorCaso(caso.id());
            repoCasos.marcarProcesada(caso.id(), true);
            return;
        }

        Metricas metricas = calcularMetricasDeTiempo(relevantes);
        String ultimoEnHablar = relevantes.get(relevantes.size() - 1).esCliente() ? ULTIMO_CLIENTE : ULTIMO_ASESOR_O_BOT;

        var ia = analizador.analizar(relevantes);
        ResultadoAnalisisIa r = ia.resultado().conMunicipio(normalizadorMunicipio.normalizar(ia.resultado().municipio()));

        Abandono abandonoCalculado = calcularAbandono(r.resultado(), ultimoEnHablar, r.tipoUltimoMensajeEmpresa());

        List<String> banderas = banderasDeCalidad(metricas, r, abandonoCalculado, ultimoEnHablar);

        repositorioAnalisis.guardar(caso.id(), idContactoConocido, r, abandonoCalculado.abandono(),
                abandonoCalculado.abandonadoPor(), banderas, metricas.primerMensaje(), metricas.primeraRespuesta(),
                metricas.cierre(), caso.archivadaEn(), caso.esDeAds(), ia.modeloUsado());

        repoCasos.marcarProcesada(caso.id(), true);
    }

    /**
     * Turnos del caso, ya filtrados a los relevantes (fallback a los que no son
     * encuesta). Optional.empty() = no hay nada que analizar (el llamador lo
     * descarta).
     */
    private Optional<List<TurnoParseado>> relevantesDelCaso(Caso caso, RepositorioConversaciones repoConversaciones,
                                                            ConfiguracionDeTenant cfg) {
        List<Turno> turnosDelCaso = repoConversaciones.listarTurnos(caso.conversacionId()).stream()
                .filter(t -> t.orden() >= caso.turnoOrdenInicio() && t.orden() <= caso.turnoOrdenFin())
                .sorted(Comparator.comparingInt(Turno::orden))
                .toList();
        if (turnosDelCaso.isEmpty()) {
            return Optional.empty();
        }

        FiltroDeRelevancia filtro = new FiltroDeRelevancia(cfg.perfil());
        List<TurnoParseado> paraCalculo = turnosDelCaso.stream()
                .map(t -> new TurnoParseado(nombreParaCalculo(t), "", t.ocurridoEn(), t.mensaje(), t.autor()))
                .toList();

        List<TurnoParseado> relevantes = paraCalculo.stream()
                .filter(t -> !filtro.esTurnoNoRelevante(t.mensaje())).toList();
        if (relevantes.isEmpty()) {
            relevantes = paraCalculo.stream().filter(t -> !filtro.esTurnoDeEncuesta(t.mensaje())).toList();
        }
        return relevantes.isEmpty() ? Optional.empty() : Optional.of(relevantes);
    }

    private static String nombreParaCalculo(Turno t) {
        if (t.nombreAutor() != null) {
            return t.nombreAutor();
        }
        return t.autor() == AutorTurno.CLIENTE ? "Cliente" : "Asesor/Bot";
    }

    private List<String> banderasDeCalidad(Metricas metricas, ResultadoAnalisisIa r, Abandono abandono,
                                           String ultimoEnHablar) {
        boolean sinRespuesta = abandono.abandonadoPor() == AbandonadoPor.ASESOR
                && ULTIMO_CLIENTE.equals(ultimoEnHablar);
        List<String> banderas = new ArrayList<>();
        if (metricas.esperoDemasiado()) {
            banderas.add("espero_demasiado");
        }
        if (sinRespuesta) {
            banderas.add("sin_respuesta");
        }
        if (Boolean.TRUE.equals(r.tratoInadecuado())) {
            banderas.add("trato_inadecuado");
        }
        if (r.gestionPendiente() && abandono.abandonadoPor() != AbandonadoPor.CLIENTE) {
            banderas.add("gestion_pendiente");
        }
        return banderas;
    }

    private record Abandono(boolean abandono, AbandonadoPor abandonadoPor) {
    }

    private Abandono calcularAbandono(Resultado resultado, String ultimoEnHablar, String tipoUltimoMensajeEmpresa) {
        if (resultado != Resultado.NO_RESUELTO) {
            return new Abandono(false, null);
        }
        if (ULTIMO_CLIENTE.equals(ultimoEnHablar)) {
            return new Abandono(true, AbandonadoPor.ASESOR);
        }
        if ("promesa_incumplida".equals(tipoUltimoMensajeEmpresa)) {
            return new Abandono(true, AbandonadoPor.ASESOR);
        }
        if (ULTIMO_ASESOR_O_BOT.equals(ultimoEnHablar)) {
            return new Abandono(true, AbandonadoPor.CLIENTE);
        }
        return new Abandono(true, AbandonadoPor.ASESOR);
    }

    private record Metricas(boolean esperoDemasiado, OffsetDateTime primerMensaje, OffsetDateTime primeraRespuesta,
                             OffsetDateTime cierre) {
    }

    private Metricas calcularMetricasDeTiempo(List<TurnoParseado> turnos) {
        long minutosEsperaMax = 0;
        TurnoParseado primerMensajeCliente = turnos.stream().filter(TurnoParseado::esCliente).findFirst().orElse(null);
        TurnoParseado primeraRespuesta = turnos.stream().filter(t -> !t.esCliente()).findFirst().orElse(null);
        TurnoParseado ultimo = turnos.get(turnos.size() - 1);

        for (int i = 0; i < turnos.size(); i++) {
            TurnoParseado actual = turnos.get(i);
            if (actual.esCliente() && actual.fecha() != null) {
                TurnoParseado respuesta = siguienteRespuestaDeEmpresa(turnos, i);
                if (respuesta != null && respuesta.fecha() != null) {
                    long minutos = Duration.between(actual.fecha(), respuesta.fecha()).toMinutes();
                    minutosEsperaMax = Math.max(minutosEsperaMax, minutos);
                }
            }
        }

        return new Metricas(minutosEsperaMax > MINUTOS_ESPERA_UMBRAL,
                primerMensajeCliente != null ? primerMensajeCliente.fecha() : null,
                primeraRespuesta != null ? primeraRespuesta.fecha() : null,
                ultimo.fecha());
    }

    /** Primer turno de la empresa (no cliente) despues de la posicion {@code i}, o null. */
    private static TurnoParseado siguienteRespuestaDeEmpresa(List<TurnoParseado> turnos, int i) {
        for (int j = i + 1; j < turnos.size(); j++) {
            if (!turnos.get(j).esCliente()) {
                return turnos.get(j);
            }
        }
        return null;
    }
}
