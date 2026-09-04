package com.marcablanca.platform.empresas.application;

import java.util.Optional;

/**
 * Guarda, durante la duracion de un request, cual es la empresa activa
 * (su "identificador", el mismo que llega en el login -- ver ADR 0001
 * del modulo usuarios).
 *
 * IMPORTANTE: quien establezca el valor es responsable de limpiarlo al
 * terminar el request (en un finally), porque el servidor reutiliza
 * threads entre peticiones.
 */
public final class ContextoEmpresaActual {

    private static final ThreadLocal<String> IDENTIFICADOR = new ThreadLocal<>();

    private ContextoEmpresaActual() {
    }

    public static void establecer(String identificadorEmpresa) {
        IDENTIFICADOR.set(identificadorEmpresa);
    }

    public static Optional<String> obtener() {
        return Optional.ofNullable(IDENTIFICADOR.get());
    }

    public static void limpiar() {
        IDENTIFICADOR.remove();
    }
}
