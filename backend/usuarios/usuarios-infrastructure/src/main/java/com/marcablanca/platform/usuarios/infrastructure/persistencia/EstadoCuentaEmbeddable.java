package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.OffsetDateTime;

@Embeddable
public class EstadoCuentaEmbeddable {

    @Column(name = "es_activo", nullable = false)
    private boolean activo;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private OffsetDateTime bloqueadoHasta;

    protected EstadoCuentaEmbeddable() {}

    public EstadoCuentaEmbeddable(boolean activo, int intentosFallidos, OffsetDateTime bloqueadoHasta) {
        this.activo = activo;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public boolean isActivo() { return activo; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public OffsetDateTime getBloqueadoHasta() { return bloqueadoHasta; }
}