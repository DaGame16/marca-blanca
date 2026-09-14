package com.marcablanca.platform.roles.domain;

import java.util.UUID;

public class Rol {

    private final Long id;
    private final UUID uuid;
    private String nombre;
    private String descripcion;
    private final boolean esDelSistema;

    public Rol(Long id, UUID uuid, String nombre, String descripcion, boolean esDelSistema) {
        this.id = id;
        this.uuid = uuid;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.esDelSistema = esDelSistema;
    }

    public static Rol nuevo(String nombre, String descripcion) {
        return new Rol(null, null, nombre, descripcion, false);
    }

    public void actualizar(String nombre, String descripcion) {
        if (esDelSistema) {
            throw new RolDelSistemaException(this.nombre);
        }
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void verificarEliminable() {
        if (esDelSistema) {
            throw new RolDelSistemaException(this.nombre);
        }
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEsDelSistema() {
        return esDelSistema;
    }
}
