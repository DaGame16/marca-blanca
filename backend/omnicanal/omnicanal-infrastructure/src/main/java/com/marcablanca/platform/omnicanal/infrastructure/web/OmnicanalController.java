package com.marcablanca.platform.omnicanal.infrastructure.web;

import com.marcablanca.platform.omnicanal.application.port.in.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/** Protegido por JWT (regla por defecto de SecurityConfig: cualquier ruta no listada exige autenticacion). */
@RestController
@RequestMapping("/api/v1/omnicanal")
public class OmnicanalController {

    private final ConsultarConversaciones consultarConversaciones;
    private final ConsultarAnalisisDeCasos consultarAnalisisDeCasos;
    private final ConsultarReportesOmnicanal consultarReportesOmnicanal;
    private final GestionarReprocesamiento gestionarReprocesamiento;
    private final EjecutarBackfillDeAds ejecutarBackfillDeAds;

    public OmnicanalController(ConsultarConversaciones consultarConversaciones,
                                ConsultarAnalisisDeCasos consultarAnalisisDeCasos,
                                ConsultarReportesOmnicanal consultarReportesOmnicanal,
                                GestionarReprocesamiento gestionarReprocesamiento,
                                EjecutarBackfillDeAds ejecutarBackfillDeAds) {
        this.consultarConversaciones = consultarConversaciones;
        this.consultarAnalisisDeCasos = consultarAnalisisDeCasos;
        this.consultarReportesOmnicanal = consultarReportesOmnicanal;
        this.gestionarReprocesamiento = gestionarReprocesamiento;
        this.ejecutarBackfillDeAds = ejecutarBackfillDeAds;
    }

    private OffsetDateTime aInicioDelDia(LocalDate fecha) {
        return fecha == null ? null : fecha.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private OffsetDateTime aFinDelDia(LocalDate fecha) {
        return fecha == null ? null : fecha.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    @GetMapping("/conversaciones")
    public Object listarConversaciones(@RequestParam(required = false) String contactId,
            @RequestParam(required = false) Integer pagina, @RequestParam(required = false) Integer porPagina,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return consultarConversaciones.listarRecientes(contactId, pagina, porPagina, aInicioDelDia(desde), aFinDelDia(hasta));
    }

    @GetMapping("/contactos/{idContacto}/resumen")
    public Object resumenContacto(@PathVariable String idContacto) {
        return consultarConversaciones.resumenDeContacto(idContacto);
    }

    @GetMapping("/analisis")
    public Object listarAnalisis(@RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer porPagina,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String resultado, @RequestParam(required = false) String motivoContacto,
            @RequestParam(required = false) Boolean abandono, @RequestParam(required = false) String abandonadoPor) {
        return consultarAnalisisDeCasos.listar(pagina, porPagina, aInicioDelDia(desde), aFinDelDia(hasta), resultado,
                motivoContacto, abandono, abandonadoPor);
    }

    @GetMapping("/analisis/{id}")
    public Object detalleAnalisis(@PathVariable String id) {
        return consultarAnalisisDeCasos.detalle(id);
    }

    @GetMapping("/estadisticas")
    public Object estadisticas(@RequestParam(required = false, defaultValue = "dia") String agrupacion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        OffsetDateTime h = hasta == null ? OffsetDateTime.now() : aFinDelDia(hasta);
        OffsetDateTime d = desde == null ? h.minusDays(30) : aInicioDelDia(desde);
        return consultarReportesOmnicanal.estadisticas(agrupacion, d, h);
    }

    @GetMapping("/reportes/sentimiento")
    public Object reporteSentimiento(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return consultarReportesOmnicanal.distribucionSentimientoOmnicanal(aInicioDelDia(desde), aFinDelDia(hasta));
    }

    @GetMapping("/reportes/soporte")
    public Object reporteSoporte(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return consultarReportesOmnicanal.resumenSoporteOmnicanal(aInicioDelDia(desde), aFinDelDia(hasta));
    }

    @GetMapping("/reportes/ventas")
    public Object reporteVentas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return consultarReportesOmnicanal.resumenVentasOmnicanal(aInicioDelDia(desde), aFinDelDia(hasta));
    }

    @GetMapping("/reportes/ads")
    public Object reporteAds(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return consultarReportesOmnicanal.resumenAdsOmnicanal(aInicioDelDia(desde), aFinDelDia(hasta));
    }

    @PostMapping("/analisis-ia/reintentar")
    public Object reintentarAnalisis() {
        return gestionarReprocesamiento.reprocesarPendientes();
    }

    @PostMapping("/analisis-ia/reprocesar-todo")
    public Object reprocesarTodo(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false, defaultValue = "true") boolean soloFaltantes) {
        return gestionarReprocesamiento.reprocesarTodo(aInicioDelDia(desde), aFinDelDia(hasta), soloFaltantes);
    }

    @GetMapping("/analisis-ia/estado-reproceso")
    public Object estadoReproceso(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return gestionarReprocesamiento.estadoReproceso(aInicioDelDia(desde), aFinDelDia(hasta));
    }

    /**
     * Reconstruye "viene de ads" consultando LIWA por contacto. Corre en
     * linea (accion puntual del tenant); para una empresa con muchos contactos
     * puede tardar -- si hace falta se le agrega paginado/resumabilidad.
     */
    @PostMapping("/analisis-ia/backfill-ads")
    public Object backfillAds() {
        return ejecutarBackfillDeAds.ejecutar();
    }

    @GetMapping("/analisis-ia/pendientes")
    public Object pendientes(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false, defaultValue = "false") boolean conIds) {
        return gestionarReprocesamiento.contarPendientes(aInicioDelDia(desde), aFinDelDia(hasta), conIds);
    }
}
