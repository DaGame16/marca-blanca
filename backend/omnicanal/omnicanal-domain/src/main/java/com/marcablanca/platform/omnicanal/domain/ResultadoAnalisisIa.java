package com.marcablanca.platform.omnicanal.domain;

import java.util.List;

/** Lo que devuelve el analisis de IA, ANTES de guardarse -- sin id/casoId todavia. */
public record ResultadoAnalisisIa(
        String razonamiento, String motivoContacto, String submotivo, String categoriaOficina,
        String municipio, String barrio, String areaDestino,
        String resumenMotivo, String resumenDesenlace,
        String sentimientoInicial, String sentimientoFinal,
        Resultado resultado, String tipoUltimoMensajeEmpresa,
        Boolean fcr, String esfuerzoCliente, List<String> temas,
        Boolean oportunidadVenta, Boolean ventaConfirmadaEnTexto, Boolean tratoInadecuado,
        boolean gestionPendiente, boolean revisarLimite) {
}
