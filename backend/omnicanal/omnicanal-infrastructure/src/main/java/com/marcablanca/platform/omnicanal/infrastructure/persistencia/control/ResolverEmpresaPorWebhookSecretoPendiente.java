package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import com.marcablanca.platform.omnicanal.application.port.out.ResolverEmpresaPorWebhookSecreto;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementacion ACTIVA hoy -- responde con un error claro en vez de dejar
 * cualquier webhook pasar. Reemplazar por ResolverEmpresaPorWebhookSecretoJpa
 * (agregandole @Component) apenas exista tbl_empresas_omnicanal.
 */
@Component
class ResolverEmpresaPorWebhookSecretoPendiente implements ResolverEmpresaPorWebhookSecreto {

    @Override
    public Optional<String> resolverIdentificadorEmpresa(String webhookSecret) {
        throw new IllegalStateException(
                "Omnicanal: tbl_empresas_omnicanal todavia no existe (pendiente de Leidi). "
                        + "El webhook no puede resolver empresas hasta que esa tabla se cree y se active "
                        + "ResolverEmpresaPorWebhookSecretoJpa -- ver comentario en EmpresaOmnicanalEntity.");
    }

    @Override
    public Optional<String> resolverLiwaApiToken(String identificadorEmpresa) {
        return Optional.empty();
    }
}
