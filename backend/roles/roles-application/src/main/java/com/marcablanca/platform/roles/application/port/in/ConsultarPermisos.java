package com.marcablanca.platform.roles.application.port.in;

import com.marcablanca.platform.roles.domain.Permiso;

import java.util.List;

public interface ConsultarPermisos {

    List<Permiso> listar();
}
