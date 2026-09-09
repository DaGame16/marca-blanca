package com.marcablanca.platform.consola.domain;

import java.util.UUID;

/**
 * Agregado de identidad de un operador de la plataforma. Vive en la base de
 * control ({@code plataforma.tbl_operadores}), nunca en la base de una empresa.
 *
 * La contrasena solo entra ya cifrada: el dominio no conoce el algoritmo, solo
 * guarda el hash y la marca de "temporal" que obliga a cambiarla en el primer
 * ingreso.
 */
public class Operador {

    private final UUID id;
    private final String correo;
    private String nombre;
    private String hashContrasena;
    private RolOperador rol;
    private boolean activo;
    private boolean contrasenaTemporal;

    public Operador(UUID id, String correo, String nombre, String hashContrasena,
                    RolOperador rol, boolean activo, boolean contrasenaTemporal) {
        if (id == null) {
            throw new IllegalArgumentException("El operador necesita un id.");
        }
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El operador necesita un correo.");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El operador necesita un rol.");
        }
        this.id = id;
        this.correo = correo.trim().toLowerCase();
        this.nombre = nombre;
        this.hashContrasena = hashContrasena;
        this.rol = rol;
        this.activo = activo;
        this.contrasenaTemporal = contrasenaTemporal;
    }

    /** Alta de un operador nuevo: contrasena ya cifrada y marcada como temporal. */
    public static Operador crear(UUID id, String correo, String nombre, String hashContrasena, RolOperador rol) {
        return new Operador(id, correo, nombre, hashContrasena, rol, true, true);
    }

    /** Fija una contrasena nueva (ya cifrada) y levanta la marca de temporal. */
    public void cambiarContrasena(String nuevoHash) {
        if (nuevoHash == null || nuevoHash.isBlank()) {
            throw new IllegalArgumentException("El hash de la nueva contrasena no puede estar vacio.");
        }
        this.hashContrasena = nuevoHash;
        this.contrasenaTemporal = false;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void exigirActivo() {
        if (!activo) {
            throw new OperadorInactivoException(correo);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public RolOperador getRol() {
        return rol;
    }

    public boolean estaActivo() {
        return activo;
    }

    public boolean debeCambiarContrasena() {
        return contrasenaTemporal;
    }
}
