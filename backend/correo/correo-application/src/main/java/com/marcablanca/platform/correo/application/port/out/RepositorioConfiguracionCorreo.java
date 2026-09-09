package com.marcablanca.platform.correo.application.port.out;

import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioConfiguracionCorreo {

    ConfiguracionSmtp crear(String remitenteNombre, String remitenteCorreo, String responderA, String host,
                             int puerto, String usuario, String secretoRef, String seguridad);

    ConfiguracionSmtp actualizar(UUID id, String remitenteNombre, String remitenteCorreo, String responderA,
                                  String host, int puerto, String usuario, String secretoRef, String seguridad);

    /** Pone esActiva=false en todas las filas EXCEPTO la de este id. */
    void desactivarTodasMenos(UUID id);

    ConfiguracionSmtp marcarActiva(UUID id);

    Optional<ConfiguracionSmtp> buscarPorId(UUID id);

    List<ConfiguracionSmtp> listarTodas();

    void eliminar(UUID id);
}
