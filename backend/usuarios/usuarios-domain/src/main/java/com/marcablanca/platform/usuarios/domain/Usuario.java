package com.marcablanca.platform.usuarios.domain;

import com.marcablanca.platform.usuarios.domain.port.out.CifradorDeContrasenas;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Usuario {

    private static final int MAX_INTENTOS_FALLIDOS = 5;

    private final Long id;
    private final UUID uuid;
    private final Correo correo;
    private HashContrasena hashContrasena;
    private String nombreCompleto;
    private boolean activo;
    private int intentosFallidos;
    private OffsetDateTime bloqueadoHasta;

    public Usuario(Long id, UUID uuid, Correo correo, HashContrasena hashContrasena,
                   String nombreCompleto, boolean activo,
                   int intentosFallidos, OffsetDateTime bloqueadoHasta) {
        this.id = id;
        this.uuid = uuid;
        this.correo = correo;
        this.hashContrasena = hashContrasena;
        this.nombreCompleto = nombreCompleto;
        this.activo = activo;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    /** Constructor para usuario nuevo (aun sin id/uuid, los asigna la base de datos). */
    public static Usuario nuevo(Correo correo, HashContrasena hashContrasena, String nombreCompleto) {
        return new Usuario(null, null, correo, hashContrasena, nombreCompleto, true, 0, null);
    }

    /**
     * Regla de negocio central del login: valida estado y credenciales, o lanza la excepcion que corresponda.
     * Mantiene el mismo contrato de excepciones que antes (CredencialesInvalidasException /
     * UsuarioNoDisponibleException) para no romper AutenticarUsuarioService en el modulo autenticacion.
     */
    public void verificarCredenciales(Contrasena contrasenaCandidata, CifradorDeContrasenas cifrador) {
        if (!activo) {
            throw new UsuarioNoDisponibleException(EstadoUsuario.INACTIVO);
        }
        if (estaBloqueado()) {
            throw new UsuarioNoDisponibleException(EstadoUsuario.BLOQUEADO);
        }
        if (!cifrador.verificar(contrasenaCandidata, hashContrasena)) {
            registrarIntentoFallido();
            throw new CredencialesInvalidasException();
        }
        reiniciarIntentosFallidos();
    }

    public boolean estaBloqueado() {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(OffsetDateTime.now());
    }

    private void registrarIntentoFallido() {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS_FALLIDOS) {
            bloqueadoHasta = OffsetDateTime.now().plusMinutes(15);
        }
    }

    private void reiniciarIntentosFallidos() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
    }

    // --- Operaciones del CRUD ---

    public void actualizarDatos(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public void cambiarContrasena(HashContrasena nuevoHash) {
        this.hashContrasena = nuevoHash;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    // --- Getters ---

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public Correo getCorreo() { return correo; }
    public String getNombreCompleto() { return nombreCompleto; }
    public boolean isActivo() { return activo; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public OffsetDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public HashContrasena getHashContrasena() { return hashContrasena; }
}