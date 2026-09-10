package com.marcablanca.platform.omnicanal.application;

import com.marcablanca.platform.omnicanal.application.port.out.AnalizadorDeConversacion;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioCasos;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal.ConfiguracionDeTenant;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones;
import com.marcablanca.platform.omnicanal.domain.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pieza compartida entre ingesta y reprocesamiento: dado un Caso, arma los
 * turnos relevantes (con el vocabulario de la empresa activa), llama al puerto
 * de IA, calcula abandono, normaliza el municipio y guarda. Se separo para no
 * duplicarla entre los 2 servicios que la usan.
 */
public class RepositorioAnalisisEscritor {

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
        FiltroDeRelevancia filtro = new FiltroDeRelevancia(cfg.perfil());
        NormalizadorDeMunicipio normalizadorMunicipio = new NormalizadorDeMunicipio(cfg.perfil());

        List<Turno> turnosDelCaso = repoConversaciones.listarTurnos(caso.conversacionId()).stream()
                .filter(t -> t.orden() >= caso.turnoOrdenInicio() && t.orden() <= caso.turnoOrdenFin())
                .sorted(java.util.Comparator.comparingInt(Turno::orden))
                .toList();

        if (turnosDelCaso.isEmpty()) {
            repositorioAnalisis.eliminarPorCaso(caso.id());
            repoCasos.marcarProcesada(caso.id(), true);
            return;
        }

        List<TurnoParseado> paraCalculo = turnosDelCaso.stream()
                .map(t -> new TurnoParseado(
                        t.nombreAutor() != null ? t.nombreAutor() : (t.autor() == AutorTurno.CLIENTE ? "Cliente" : "Asesor/Bot"),
                        "", t.ocurridoEn(), t.mensaje(), t.autor()))
                .toList();

        List<TurnoParseado> relevantes = paraCalculo.stream()
                .filter(t -> !filtro.esTurnoNoRelevante(t.mensaje())).toList();
        if (relevantes.isEmpty()) {
            relevantes = paraCalculo.stream().filter(t -> !filtro.esTurnoDeEncuesta(t.mensaje())).toList();
        }
        if (relevantes.isEmpty()) {
            repositorioAnalisis.eliminarPorCaso(caso.id());
            repoCasos.marcarProcesada(caso.id(), true);
            return;
        }

        var metricas = calcularMetricasDeTiempo(relevantes);
        String ultimoEnHablar = relevantes.get(relevantes.size() - 1).esCliente() ? "cliente" : "asesor_o_bot";

        var ia = analizador.analizar(relevantes);
        ResultadoAnalisisIa r = ia.resultado().conMunicipio(normalizadorMunicipio.normalizar(ia.resultado().municipio()));

        var abandonoCalculado = calcularAbandono(r.resultado(), ultimoEnHablar, r.tipoUltimoMensajeEmpresa());
        boolean sinRespuesta = abandonoCalculado.abandonadoPor() == AbandonadoPor.ASESOR
                && "cliente".equals(ultimoEnHablar);

        List<String> banderas = new ArrayList<>();
        if (metricas.esperoDemasiado()) banderas.add("espero_demasiado");
        if (sinRespuesta) banderas.add("sin_respuesta");
        if (Boolean.TRUE.equals(r.tratoInadecuado())) banderas.add("trato_inadecuado");
        if (r.gestionPendiente() && abandonoCalculado.abandonadoPor() != AbandonadoPor.CLIENTE) {
            banderas.add("gestion_pendiente");
        }

        repositorioAnalisis.guardar(caso.id(), idContactoConocido, r, abandonoCalculado.abandono(),
                abandonoCalculado.abandonadoPor(), banderas, metricas.primerMensaje(), metricas.primeraRespuesta(),
                metricas.cierre(), caso.archivadaEn(), caso.esDeAds(), ia.modeloUsado());

        repoCasos.marcarProcesada(caso.id(), true);
    }

    private record Abandono(boolean abandono, AbandonadoPor abandonadoPor) {
    }

    private Abandono calcularAbandono(Resultado resultado, String ultimoEnHablar, String tipoUltimoMensajeEmpresa) {
        if (resultado != Resultado.NO_RESUELTO) {
            return new Abandono(false, null);
        }
        if ("cliente".equals(ultimoEnHablar)) {
            return new Abandono(true, AbandonadoPor.ASESOR);
        }
        if ("promesa_incumplida".equals(tipoUltimoMensajeEmpresa)) {
            return new Abandono(true, AbandonadoPor.ASESOR);
        }
        if ("asesor_o_bot".equals(ultimoEnHablar)) {
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
            if (!actual.esCliente() || actual.fecha() == null) continue;
            TurnoParseado siguienteRespuesta = null;
            for (int j = i + 1; j < turnos.size(); j++) {
                if (!turnos.get(j).esCliente()) {
                    siguienteRespuesta = turnos.get(j);
                    break;
                }
            }
            if (siguienteRespuesta == null || siguienteRespuesta.fecha() == null) continue;
            long minutos = java.time.Duration.between(actual.fecha(), siguienteRespuesta.fecha()).toMinutes();
            if (minutos > minutosEsperaMax) minutosEsperaMax = minutos;
        }

        return new Metricas(minutosEsperaMax > 30,
                primerMensajeCliente != null ? primerMensajeCliente.fecha() : null,
                primeraRespuesta != null ? primeraRespuesta.fecha() : null,
                ultimo.fecha());
    }
}
