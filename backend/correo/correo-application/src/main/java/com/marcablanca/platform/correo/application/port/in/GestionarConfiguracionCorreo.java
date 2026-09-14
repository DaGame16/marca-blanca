package com.marcablanca.platform.correo.application.port.in;

import com.marcablanca.platform.correo.application.ComandoConfiguracionSmtp;
import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;

import java.util.List;
import java.util.UUID;

public interface GestionarConfiguracionCorreo {

    ConfiguracionSmtp crear(ComandoConfiguracionSmtp comando);

    ConfiguracionSmtp actualizar(UUID id, ComandoConfiguracionSmtp comando);

    /** Activa esta, desactiva cualquier otra que estuviera activa -- como maximo una a la vez. */
    ConfiguracionSmtp activar(UUID id);

    List<ConfiguracionSmtp> listar();

    ConfiguracionSmtp buscarPorId(UUID id);

    /** No permite borrar la que esta activa -- hay que activar otra primero. */
    void eliminar(UUID id);
}
