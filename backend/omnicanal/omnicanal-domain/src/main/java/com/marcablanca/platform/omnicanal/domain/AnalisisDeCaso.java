package com.marcablanca.platform.omnicanal.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * tbl_conversaciones_analizadas -- 1:1 con Caso. La mayoria de los campos
 * quedan como String a proposito: en la tabla real NO tienen CHECK (a
 * diferencia de "autor"), asi que forzar un enum de este lado seria mas
 * estricto que lo que la propia base exige.
 */
public record AnalisisDeCaso(
        Long id, UUID uuid, Long casoId, String idContacto,
        String areaDestino, String municipio, String barrio, String categoriaOficina,
        String motivoContacto, String submotivo,
        String resumenMotivo, String resumenDesenlace,
        String sentimientoInicial, String sentimientoFinal,
        Resultado resultado,
        Boolean fcr, String esfuerzoCliente,
        List<String> temas, List<String> banderasCalidad,
        Boolean oportunidadVenta, Boolean ventaConfirmadaEnTexto, Boolean revisarLimite,
        Boolean abandono, AbandonadoPor abandonadoPor, boolean esDeAds,
        String modeloIaUsado, String razonamiento,
        OffsetDateTime cerradoEn, OffsetDateTime primerMensajeEn, OffsetDateTime primeraRespuestaEn,
        OffsetDateTime procesadoEn) {
}
