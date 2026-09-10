package com.marcablanca.platform.omnicanal.application.port.out;

/**
 * ACL hacia modulos-empresa. Dice si la empresa activa en la peticion tiene
 * contratado y activo el modulo "omnicanal". Lo implementa un adaptador puente
 * en -infrastructure (PuenteModulosOmnicanal), que traduce hacia los puertos de
 * entrada publicos de modulos-empresa -- mismo criterio que PuenteModulosEmpresa
 * en aprovisionamiento.
 */
public interface ModuloOmnicanalHabilitado {

    /** El codigo del modulo en plataforma.tbl_modulos (ver changeset control 0011). */
    String CODIGO = "omnicanal";

    boolean paraEmpresaActual();
}
