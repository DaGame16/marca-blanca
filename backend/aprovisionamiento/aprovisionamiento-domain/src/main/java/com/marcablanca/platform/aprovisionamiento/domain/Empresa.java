package com.marcablanca.platform.aprovisionamiento.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Agregado raiz del contexto de aprovisionamiento: concentra las reglas del alta
 * y del ciclo de vida de una empresa cliente.
 *
 * La identidad estable es el uuid; el id serial de la tabla es un detalle de
 * persistencia y no vive en este objeto.
 */
public class Empresa {

    private final UUID id;
    private final Identificador identificador;
    private String nombreLegal;
    private String nombreComercial;                      // se define en personalizacion
    private String dominio;
    private String representanteLegal;
    private String correo;                               // contacto + usuario de login
    private String telefono;
    private String sitioWeb;
    private HashContrasenaMaestra hashContrasenaMaestra; // se genera al enviar el correo de bienvenida
    private EstadoEmpresa estado;

    private final List<EventoDeDominio> eventos = new ArrayList<>();

    /** Constructor de reconstruccion: lo usa el adaptador de persistencia al leer de la base. */
    public Empresa(UUID id, Identificador identificador, String nombreLegal, String nombreComercial,
                   String dominio, String representanteLegal, String correo, String telefono, String sitioWeb,
                   HashContrasenaMaestra hashContrasenaMaestra, EstadoEmpresa estado) {
        this.id = id;
        this.identificador = identificador;
        this.nombreLegal = nombreLegal;
        this.nombreComercial = nombreComercial;
        this.dominio = dominio;
        this.representanteLegal = representanteLegal;
        this.correo = correo;
        this.telefono = telefono;
        this.sitioWeb = sitioWeb;
        this.hashContrasenaMaestra = hashContrasenaMaestra;
        this.estado = estado;
    }

    /** Paso 1 del registro: la empresa queda en BORRADOR. Sin contrasena, sin modulos, sin evento. */
    public static Empresa registrar(Identificador identificador,
                                    String dominio,
                                    String nombreEmpresa,
                                    String representanteLegal,
                                    String correo,
                                    String telefono,
                                    String sitioWeb) {
        exigir(identificador != null, "El identificador es obligatorio.");
        exigirTexto(dominio, "El dominio es obligatorio.");
        exigirTexto(nombreEmpresa, "El nombre de la empresa es obligatorio.");
        exigirTexto(representanteLegal, "El representante legal es obligatorio.");
        exigirTexto(correo, "El correo es obligatorio.");
        exigirTexto(telefono, "El telefono es obligatorio.");
        exigirTexto(sitioWeb, "El sitio web es obligatorio.");

        return new Empresa(
                UUID.randomUUID(),
                identificador,
                nombreEmpresa.trim(),
                null,
                dominio.trim(),
                representanteLegal.trim(),
                correo.trim().toLowerCase(),
                telefono.trim(),
                sitioWeb.trim(),
                null,
                EstadoEmpresa.BORRADOR);
    }

    /**
     * Paso 6 del wizard: termina el registro. La empresa pasa de BORRADOR a
     * PENDIENTE_APROVISIONAMIENTO y levanta EmpresaRegistrada para que el pipeline
     * (Capa 2) la aprovisione.
     */
    public void finalizarRegistro(Set<String> modulosSeleccionados) {
        if (estado != EstadoEmpresa.BORRADOR) {
            throw new EmpresaNoModificableException(estado);
        }
        estado = EstadoEmpresa.PENDIENTE_APROVISIONAMIENTO;
        eventos.add(new EmpresaRegistrada(
                id,
                identificador.valor(),
                nombreLegal,
                nombreComercial,
                dominio,
                modulosSeleccionados == null ? Set.of() : Set.copyOf(modulosSeleccionados),
                Instant.now()));
    }

    /** La base quedo lista y la empresa entra en operacion. */
    public void activar() {
        if (estado != EstadoEmpresa.PENDIENTE_APROVISIONAMIENTO) {
            throw new EmpresaNoActivableException(estado);
        }
        estado = EstadoEmpresa.ACTIVA;
    }

    /**
     * Suspension operativa desde la consola: ACTIVA -&gt; SUSPENDIDA. La base de la
     * empresa sigue existiendo; solo se le corta el acceso. Es reversible.
     */
    public void suspender() {
        if (estado != EstadoEmpresa.ACTIVA) {
            throw new EmpresaNoSuspendibleException(estado);
        }
        estado = EstadoEmpresa.SUSPENDIDA;
    }

    /** Reactivacion desde la consola: SUSPENDIDA -&gt; ACTIVA. */
    public void reactivar() {
        if (estado != EstadoEmpresa.SUSPENDIDA) {
            throw new EmpresaNoReactivableException(estado);
        }
        estado = EstadoEmpresa.ACTIVA;
    }

    public List<EventoDeDominio> eventosPendientes() {
        return List.copyOf(eventos);
    }

    public void limpiarEventos() {
        eventos.clear();
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    public UUID getId() { return id; }
    public Identificador getIdentificador() { return identificador; }
    public String getNombreLegal() { return nombreLegal; }
    public String getNombreComercial() { return nombreComercial; }
    public String getDominio() { return dominio; }
    public String getRepresentanteLegal() { return representanteLegal; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getSitioWeb() { return sitioWeb; }
    public HashContrasenaMaestra getHashContrasenaMaestra() { return hashContrasenaMaestra; }
    public EstadoEmpresa getEstado() { return estado; }
}
