package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.marcablanca.platform.omnicanal.application.port.out.RepositorioAnalisis;
import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConversaciones.Pagina;
import com.marcablanca.platform.omnicanal.domain.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
class RepositorioAnalisisJpa implements RepositorioAnalisis {

    private final AnalisisJpaRepository analisis;

    RepositorioAnalisisJpa(AnalisisJpaRepository analisis) {
        this.analisis = analisis;
    }

    @Override
    public void guardar(Long casoId, String idContacto, ResultadoAnalisisIa r, Boolean abandono,
                         AbandonadoPor abandonadoPor, List<String> banderasCalidad, OffsetDateTime primerMensajeEn,
                         OffsetDateTime primeraRespuestaEn, OffsetDateTime cerradoEn, OffsetDateTime procesadoEn,
                         boolean esDeAds, String modeloIaUsado) {
        AnalisisEntity e = analisis.findByCasoId(casoId).orElseGet(() -> AnalisisEntity.nueva(casoId, idContacto));
        // El municipio ya viene normalizado desde RepositorioAnalisisEscritor
        // (con el perfil de la empresa activa) -- no se re-normaliza aca.
        e.setCampos(r.areaDestino(), r.municipio(), r.barrio(),
                r.categoriaOficina(), r.motivoContacto(), r.submotivo(), r.resumenMotivo(), r.resumenDesenlace(),
                r.sentimientoInicial(), r.sentimientoFinal(), r.resultado(), r.fcr(), r.esfuerzoCliente(), r.temas(),
                banderasCalidad, r.oportunidadVenta(), r.ventaConfirmadaEnTexto(), r.revisarLimite(), abandono,
                abandonadoPor, esDeAds, modeloIaUsado, r.razonamiento(), cerradoEn, primerMensajeEn,
                primeraRespuestaEn, procesadoEn);
        analisis.save(e);
    }

    @Override
    public void eliminarPorCaso(Long casoId) {
        analisis.deleteByCasoId(casoId);
    }

    @Override
    public Optional<AnalisisDeCaso> buscarPorId(String uuid) {
        try {
            return analisis.findByUuid(java.util.UUID.fromString(uuid)).map(this::mapear);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AnalisisDeCaso> buscarPorCasoId(Long casoId) {
        return analisis.findByCasoId(casoId).map(this::mapear);
    }

    @Override
    public Pagina<AnalisisDeCaso> listar(int pagina, int porPagina, OffsetDateTime desde, OffsetDateTime hasta,
                                          String resultadoTexto, String motivoContacto, Boolean abandono,
                                          String abandonadoPorTexto) {
        Resultado resultado = resultadoTexto == null ? null : Resultado.valueOf(resultadoTexto.toUpperCase());
        var page = analisis.buscar(resultado, motivoContacto, abandono, desde, hasta,
                PageRequest.of(pagina - 1, porPagina));
        return new Pagina<>(page.getTotalElements(), pagina, porPagina, Math.max(1, page.getTotalPages()),
                page.getContent().stream().map(this::mapear).toList());
    }

    @Override
    public Map<String, Long> distribucionSentimientoInicial(OffsetDateTime desde, OffsetDateTime hasta) {
        return aMapa(analisis.agruparPorSentimientoInicial(desde, hasta));
    }

    @Override
    public Map<String, Long> distribucionSentimientoFinal(OffsetDateTime desde, OffsetDateTime hasta) {
        return aMapa(analisis.agruparPorSentimientoFinal(desde, hasta));
    }

    @Override
    public long contarPorMotivoYResultado(String motivoContacto, String resultadoTexto, OffsetDateTime desde,
                                           OffsetDateTime hasta) {
        return analisis.countByMotivoContactoAndResultadoAndProcesadoEnBetween(motivoContacto,
                Resultado.valueOf(resultadoTexto.toUpperCase()), desde, hasta);
    }

    @Override
    public long contarPorMotivo(String motivoContacto, OffsetDateTime desde, OffsetDateTime hasta) {
        return analisis.countByMotivoContacto(motivoContacto);
    }

    @Override
    public long contarOportunidadVenta(boolean confirmadaEnTexto, OffsetDateTime desde, OffsetDateTime hasta) {
        return confirmadaEnTexto
                ? analisis.countByOportunidadVentaAndVentaConfirmadaEnTexto(true, true)
                : analisis.countByOportunidadVenta(true);
    }

    @Override
    public long contarPorAds(String campo, OffsetDateTime desde, OffsetDateTime hasta) {
        return analisis.countByEsDeAds(true);
    }

    @Override
    public Map<String, Long> agruparPorMotivoConAds(OffsetDateTime desde, OffsetDateTime hasta) {
        return aMapa(analisis.agruparPorMotivoConAds(desde, hasta));
    }

    @Override
    public Map<String, Long> agruparPorResultadoConAds(OffsetDateTime desde, OffsetDateTime hasta) {
        return aMapa(analisis.agruparPorResultadoConAds(desde, hasta));
    }

    private Map<String, Long> aMapa(List<Object[]> filas) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            mapa.put(fila[0] == null ? "desconocido" : fila[0].toString(), (Long) fila[1]);
        }
        return mapa;
    }

    private AnalisisDeCaso mapear(AnalisisEntity e) {
        return new AnalisisDeCaso(e.getId(), e.getUuid(), e.getCasoId(), e.getIdContacto(), e.getAreaDestino(),
                e.getMunicipio(), e.getBarrio(), e.getCategoriaOficina(), e.getMotivoContacto(), e.getSubmotivo(),
                e.getResumenMotivo(), e.getResumenDesenlace(), e.getSentimientoInicial(), e.getSentimientoFinal(),
                e.getResultado(), e.getFcr(), e.getEsfuerzoCliente(), e.getTemas(), e.getBanderasCalidad(),
                e.getOportunidadVenta(), e.getVentaConfirmadaEnTexto(), e.getRevisarLimite(), e.getAbandono(),
                e.getAbandonadoPor(), e.isEsDeAds(), e.getModeloIaUsado(), e.getRazonamiento(), e.getCerradoEn(),
                e.getPrimerMensajeEn(), e.getPrimeraRespuestaEn(), e.getProcesadoEn());
    }
}
