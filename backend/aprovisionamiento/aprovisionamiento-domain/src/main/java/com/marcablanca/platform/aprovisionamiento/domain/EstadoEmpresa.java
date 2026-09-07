package com.marcablanca.platform.aprovisionamiento.domain;

/** Ciclo de vida de una empresa. Coincide con el CHECK ck_empresas_estado de tbl_empresas. */
public enum EstadoEmpresa {
    PENDIENTE_APROVISIONAMIENTO,
    ACTIVA,
    SUSPENDIDA,
    INACTIVA
}