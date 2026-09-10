package com.marcablanca.platform.omnicanal.application.port.out;

import java.util.Optional;

/**
 * Puerto propio -- resuelve de que empresa es un webhook entrante a partir
 * de su secreto (no hay JWT ni formulario: el secreto ES el dato de tenant).
 * Devuelve el "identificador" (slug) de la empresa, o vacio si el secreto no
 * corresponde a ninguna empresa con omnicanal aprovisionado.
 */
public interface ResolverEmpresaPorWebhookSecreto {
    Optional<String> resolverIdentificadorEmpresa(String webhookSecret);
}
