package com.marcablanca.platform.aprovisionamiento.application.port.in;

import com.marcablanca.platform.aprovisionamiento.application.ComandoRegistrarEmpresa;

import java.util.UUID;

/** Capa 1: da de alta la empresa y solicita su aprovisionamiento. Devuelve el uuid de la empresa. */
public interface RegistrarEmpresa {
    UUID ejecutar(ComandoRegistrarEmpresa comando);
}