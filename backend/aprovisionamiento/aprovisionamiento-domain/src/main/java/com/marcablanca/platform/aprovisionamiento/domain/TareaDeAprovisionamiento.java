package com.marcablanca.platform.aprovisionamiento.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Estado de la saga de aprovisionamiento de UNA empresa.
 * paso() es el ultimo checkpoint COMPLETADO; el pipeline reanuda desde el siguiente.
 * Toda la logica de reintentos/backoff vive aca, no en el servicio.
 */
public class TareaDeAprovisionamiento {

    private static final int MAX_INTENTOS_POR_DEFECTO = 5;
    private static final long BACKOFF_TOPE_SEGUNDOS = 300;

    private final UUID id;
    private final UUID empresaId;
    private final String nombreBd;
    private PasoDeAprovisionamiento paso;
    private EstadoTarea estado;
    private int intentos;
    private final int maxIntentos;
    private String ultimoError;
    private Instant disponibleEn;

    public TareaDeAprovisionamiento(UUID id, UUID empresaId, String nombreBd, PasoDeAprovisionamiento paso,
                                    EstadoTarea estado, int intentos, int maxIntentos,
                                    String ultimoError, Instant disponibleEn) {
        this.id = id;
        this.empresaId = empresaId;
        this.nombreBd = nombreBd;
        this.paso = paso;
        this.estado = estado;
        this.intentos = intentos;
        this.maxIntentos = maxIntentos;
        this.ultimoError = ultimoError;
        this.disponibleEn = disponibleEn;
    }

    /** Primera vez que se aprovisiona esta empresa. */
    public static TareaDeAprovisionamiento iniciar(UUID empresaId, String nombreBd) {
        return new TareaDeAprovisionamiento(
                UUID.randomUUID(), empresaId, nombreBd,
                PasoDeAprovisionamiento.NO_INICIADO, EstadoTarea.EN_PROGRESO,
                0, MAX_INTENTOS_POR_DEFECTO, null, Instant.now());
    }

    /** Un paso se completo bien: se guarda el checkpoint y se limpia el ultimo error. */
    public void avanzarA(PasoDeAprovisionamiento pasoCompletado) {
        this.paso = pasoCompletado;
        this.ultimoError = null;
        this.estado = pasoCompletado == PasoDeAprovisionamiento.BIENVENIDA_ENVIADA
                ? EstadoTarea.COMPLETADO
                : EstadoTarea.EN_PROGRESO;
    }

    /** Un paso fallo: sube el contador, y o bien reprograma con backoff o marca ERROR terminal. */
    public void registrarFallo(String mensaje) {
        this.intentos++;
        this.ultimoError = mensaje;
        if (intentos >= maxIntentos) {
            this.estado = EstadoTarea.ERROR;
        } else {
            this.estado = EstadoTarea.EN_PROGRESO;
            long espera = Math.min(BACKOFF_TOPE_SEGUNDOS, (long) (Math.pow(2, intentos) * 10));
            this.disponibleEn = Instant.now().plusSeconds(espera);
        }
    }

    public boolean agotoReintentos() {
        return estado == EstadoTarea.ERROR;
    }

    public UUID getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public String getNombreBd() { return nombreBd; }
    public PasoDeAprovisionamiento getPaso() { return paso; }
    public EstadoTarea getEstado() { return estado; }
    public int getIntentos() { return intentos; }
    public int getMaxIntentos() { return maxIntentos; }
    public String getUltimoError() { return ultimoError; }
    public Instant getDisponibleEn() { return disponibleEn; }
}