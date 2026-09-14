package com.marcablanca.platform.omnicanal.application.port.out;

import com.marcablanca.platform.omnicanal.domain.AnalisisDeCaso;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RepositorioAnalisis {

    // java:S107 (muchos parametros): persiste el resultado completo de un
    // analisis de caso -- combina el resultado de la IA (ResultadoAnalisisIa)
    // con metadata de seguimiento del caso (timing, abandono, ads) que no
    // pertenece al mismo objeto de dominio. No se agrupa en un record nuevo
    // sin revisar con cuidado los 2+ llamadores (aplicacion + adaptador JPA).
    void guardar(Long casoId, String idContacto, com.marcablanca.platform.omnicanal.domain.ResultadoAnalisisIa r, //NOSONAR ver comentario arriba
                 Boolean abandono, com.marcablanca.platform.omnicanal.domain.AbandonadoPor abandonadoPor,
                 java.util.List<String> banderasCalidad, OffsetDateTime primerMensajeEn,
                 OffsetDateTime primeraRespuestaEn, OffsetDateTime cerradoEn, OffsetDateTime procesadoEn,
                 boolean esDeAds, String modeloIaUsado);

    void eliminarPorCaso(Long casoId);

    /** Marca es_de_ads en el analisis de un caso, si existe (backfill de ads). */
    void marcarEsDeAdsPorCaso(Long casoId, boolean esDeAds);

    Optional<AnalisisDeCaso> buscarPorId(String uuid);

    Optional<AnalisisDeCaso> buscarPorCasoId(Long casoId);

    // java:S107: paginado + 4 filtros independientes de un listado -- misma
    // forma que ConsultarAnalisisDeCasos.listar (port in), que ya recibe
    // exactamente estos parametros del controller.
    RepositorioConversaciones.Pagina<AnalisisDeCaso> listar(int pagina, int porPagina, OffsetDateTime desde, //NOSONAR ver comentario arriba
            OffsetDateTime hasta, String resultado, String motivoContacto, Boolean abandono,
            String abandonadoPor);

    java.util.Map<String, Long> distribucionSentimientoInicial(OffsetDateTime desde, OffsetDateTime hasta);

    java.util.Map<String, Long> distribucionSentimientoFinal(OffsetDateTime desde, OffsetDateTime hasta);

    long contarPorMotivoYResultado(String motivoContacto, String resultado, OffsetDateTime desde,
            OffsetDateTime hasta);

    long contarPorMotivo(String motivoContacto, OffsetDateTime desde, OffsetDateTime hasta);

    long contarOportunidadVenta(boolean confirmadaEnTexto, OffsetDateTime desde, OffsetDateTime hasta);

    long contarPorAds(String campo, OffsetDateTime desde, OffsetDateTime hasta);

    java.util.Map<String, Long> agruparPorMotivoConAds(OffsetDateTime desde, OffsetDateTime hasta);

    java.util.Map<String, Long> agruparPorResultadoConAds(OffsetDateTime desde, OffsetDateTime hasta);
}
