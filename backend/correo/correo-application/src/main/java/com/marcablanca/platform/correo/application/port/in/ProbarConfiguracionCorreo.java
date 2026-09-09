package com.marcablanca.platform.correo.application.port.in;

import java.util.UUID;

/**
 * Manda un correo real de prueba usando UNA configuracion puntual (no
 * necesariamente la activa) -- para que un admin valide que sus datos SMTP
 * funcionan antes de activarla. A diferencia del envio normal (que se traga
 * los errores para no tumbar otros pipelines), aca el error SI se propaga:
 * el unico proposito de este caso de uso es que se vea.
 */
public interface ProbarConfiguracionCorreo {
    void ejecutar(UUID id, String destinatario);

    /**
     * Igual que {@link #ejecutar}, pero contra datos que TODAVIA no estan
     * guardados -- se usa para validar, antes de crear o editar una
     * configuracion, que de verdad se puede enviar correo con ella. Asi no
     * quedan en la tabla configuraciones "fantasma" con credenciales que no
     * sirven.
     */
    void ejecutarAdHoc(DatosConexion datos, String destinatario);

    record DatosConexion(String remitenteNombre, String remitenteCorreo, String host, int puerto,
                          String usuario, String seguridad, String clave) {
    }
}
