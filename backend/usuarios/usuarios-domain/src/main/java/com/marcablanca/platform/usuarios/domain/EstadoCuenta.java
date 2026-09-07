package com.marcablanca.platform.usuarios.domain;

import java.time.OffsetDateTime;

public class EstadoCuenta {

    private final boolean activo;
    private final int intentosFallidos;
    private final OffsetDateTime bloqueadoHasta;

    public EstadoCuenta(boolean activo, int intentosFallidos, OffsetDateTime bloqueadoHasta) {
        this.activo = activo;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public static EstadoCuenta nueva() {
        return new EstadoCuenta(true, 0, null);
    }

    public boolean isActivo() { return activo; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public OffsetDateTime getBloqueadoHasta() { return bloqueadoHasta; }
}