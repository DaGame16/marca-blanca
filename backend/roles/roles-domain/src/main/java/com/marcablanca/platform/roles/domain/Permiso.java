package com.marcablanca.platform.roles.domain;

import java.util.UUID;

/**
 * Catalogo de permisos: lo define el codigo de la plataforma, no se crea ni
 * se edita desde la aplicacion (a diferencia de Rol). Solo se consulta.
 */
public class Permiso {

    private final Long id;
    private final UUID uuid;
    private final String nombre;
    private final String descripcion;

    public Permiso(Long id, UUID uuid, String nombre, String descripcion) {
        this.id = id;
        this.uuid = uuid;
        this.nombre = nombre;
        this.descripcion = descripcion;
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
}
