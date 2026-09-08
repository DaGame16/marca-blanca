package com.marcablanca.platform.omnicanal.application.port.out;

import java.util.Optional;

/**
 * Puerto propio -- resuelve de que empresa es un webhook entrante a partir
 * de su secreto (no hay JWT ni formulario: el secreto ES el dato de tenant).
 *
 * Implementacion real pendiente de que exista tbl_empresas_omnicanal (base
 * de CONTROL) -- ver ResolverEmpresaPorWebhookSecretoPendiente mientras tanto.
 */
public interface ResolverEmpresaPorWebhookSecreto {
    Optional<String> resolverIdentificadorEmpresa(String webhookSecret);

    Optional<String> resolverLiwaApiToken(String identificadorEmpresa);
}
