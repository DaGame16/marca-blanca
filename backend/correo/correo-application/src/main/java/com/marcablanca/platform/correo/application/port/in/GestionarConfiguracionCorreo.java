package com.marcablanca.platform.correo.application.port.in;

import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;

import java.util.List;
import java.util.UUID;

public interface GestionarConfiguracionCorreo {

    /** clave: la clave SMTP en texto plano (se cifra antes de guardar) -- null significa "no cambiarla". */
    record ComandoConfiguracionSmtp(String remitenteNombre, String remitenteCorreo, String responderA,
                                     String host, int puerto, String usuario, String secretoRef,
                                     String seguridad, String clave) {
    }

    ConfiguracionSmtp crear(ComandoConfiguracionSmtp comando);

    ConfiguracionSmtp actualizar(UUID id, ComandoConfiguracionSmtp comando);

    /** Activa esta, desactiva cualquier otra que estuviera activa -- como maximo una a la vez. */
    ConfiguracionSmtp activar(UUID id);

    List<ConfiguracionSmtp> listar();

    ConfiguracionSmtp buscarPorId(UUID id);

    /** No permite borrar la que esta activa -- hay que activar otra primero. */
    void eliminar(UUID id);
}
