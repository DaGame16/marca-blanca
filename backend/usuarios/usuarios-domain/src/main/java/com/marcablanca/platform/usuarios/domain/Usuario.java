package com.marcablanca.platform.usuarios.domain;



import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class Usuario {

    private static final int MAX_INTENTOS_FALLIDOS = 5;

    private final Long id;
    private final UUID uuid;
    private final Correo correo;
    private HashContrasena hashContrasena;
    private String nombreCompleto;
    private EstadoCuenta estadoCuenta;

    public Usuario(Long id, UUID uuid, Correo correo, HashContrasena hashContrasena,
            String nombreCompleto, EstadoCuenta estadoCuenta) {
        this.id = id;
        this.uuid = uuid;
        this.correo = correo;
        this.hashContrasena = hashContrasena;
        this.nombreCompleto = nombreCompleto;
        this.estadoCuenta = estadoCuenta;
    }

    public static Usuario nuevo(Correo correo, HashContrasena hashContrasena, String nombreCompleto) {
        return new Usuario(null, null, correo, hashContrasena, nombreCompleto, EstadoCuenta.nueva());
    }

    public void verificarCredenciales(boolean contrasenaCorrecta) {
        if (!estadoCuenta.isActivo()) {
            throw new UsuarioNoDisponibleException(EstadoUsuario.INACTIVO);
        }
        if (estaBloqueado()) {
            throw new UsuarioNoDisponibleException(EstadoUsuario.BLOQUEADO);
        }
        if (!contrasenaCorrecta) {
            registrarIntentoFallido();
            throw new CredencialesInvalidasException();
        }
        reiniciarIntentosFallidos();
    }

    public boolean estaBloqueado() {
        OffsetDateTime bloqueadoHasta = estadoCuenta.getBloqueadoHasta();
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
    }

    private void registrarIntentoFallido() {
        int intentos = estadoCuenta.getIntentosFallidos() + 1;
        OffsetDateTime bloqueadoHasta = intentos >= MAX_INTENTOS_FALLIDOS
                ? OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(15)
                : estadoCuenta.getBloqueadoHasta();
        estadoCuenta = new EstadoCuenta(estadoCuenta.isActivo(), intentos, bloqueadoHasta);
    }

    private void reiniciarIntentosFallidos() {
        estadoCuenta = new EstadoCuenta(estadoCuenta.isActivo(), 0, null);
    }

    public void actualizarDatos(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public void cambiarContrasena(HashContrasena nuevoHash) {
        this.hashContrasena = nuevoHash;
    }

    public void activar() {
        estadoCuenta = new EstadoCuenta(true, estadoCuenta.getIntentosFallidos(), estadoCuenta.getBloqueadoHasta());
    }

    public void desactivar() {
        estadoCuenta = new EstadoCuenta(false, estadoCuenta.getIntentosFallidos(), estadoCuenta.getBloqueadoHasta());
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Correo getCorreo() {
        return correo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public boolean isActivo() {
        return estadoCuenta.isActivo();
    }

    public int getIntentosFallidos() {
        return estadoCuenta.getIntentosFallidos();
    }

    public OffsetDateTime getBloqueadoHasta() {
        return estadoCuenta.getBloqueadoHasta();
    }

    public HashContrasena getHashContrasena() {
        return hashContrasena;
    }
}