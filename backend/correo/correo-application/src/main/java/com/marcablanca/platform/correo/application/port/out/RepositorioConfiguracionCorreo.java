package com.marcablanca.platform.correo.application.port.out;

import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioConfiguracionCorreo {

    ConfiguracionSmtp crear(String remitenteNombre, String remitenteCorreo, String responderA, String host,
                             int puerto, String usuario, String secretoRef, String seguridad, String clave);

    ConfiguracionSmtp actualizar(UUID id, String remitenteNombre, String remitenteCorreo, String responderA,
                                  String host, int puerto, String usuario, String secretoRef, String seguridad,
                                  String clave);

    /** Descifra y devuelve la clave SMTP de esta config -- SOLO para uso interno al enviar, nunca se expone via API. */
    Optional<String> obtenerClaveDescifrada(UUID id);

    /** La config que se usa para enviar de verdad -- como maximo una fila con esActiva=true. */
    Optional<ConfiguracionSmtp> buscarActiva();

    /** Pone esActiva=false en todas las filas EXCEPTO la de este id. */
    void desactivarTodasMenos(UUID id);

    ConfiguracionSmtp marcarActiva(UUID id);

    Optional<ConfiguracionSmtp> buscarPorId(UUID id);

    List<ConfiguracionSmtp> listarTodas();

    void eliminar(UUID id);

    /** true si YA existe otra fila (distinta de excluirId) con este remitente -- sin distinguir mayus/minus. */
    boolean existeConCorreo(String remitenteCorreo, UUID excluirId);
}
