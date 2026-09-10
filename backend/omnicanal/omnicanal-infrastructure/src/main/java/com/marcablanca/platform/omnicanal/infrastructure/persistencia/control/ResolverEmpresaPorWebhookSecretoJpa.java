package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import com.marcablanca.platform.omnicanal.application.port.out.ResolverEmpresaPorWebhookSecreto;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resuelve el tenant de un webhook entrante contra
 * plataforma.tbl_empresas_omnicanal (base de CONTROL): del secreto del header
 * saca el empresa_id, y de ahi el "identificador" (slug) que va a
 * ContextoEmpresaActual. Devuelve Optional.empty() para un secreto ausente,
 * vacio o desconocido -- el flujo de ingesta lo traduce a 401.
 */
@Component
class ResolverEmpresaPorWebhookSecretoJpa implements ResolverEmpresaPorWebhookSecreto {

    private final EmpresaOmnicanalJpaRepository empresasOmnicanal;
    private final EmpresaRefDeOmnicanalJpaRepository empresasRef;

    ResolverEmpresaPorWebhookSecretoJpa(EmpresaOmnicanalJpaRepository empresasOmnicanal,
                                        EmpresaRefDeOmnicanalJpaRepository empresasRef) {
        this.empresasOmnicanal = empresasOmnicanal;
        this.empresasRef = empresasRef;
    }

    @Override
    public Optional<String> resolverIdentificadorEmpresa(String webhookSecret) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            return Optional.empty();
        }
        return empresasOmnicanal.findByWebhookSecret(webhookSecret)
                .flatMap(config -> empresasRef.findById(config.getEmpresaId()))
                .map(EmpresaRefDeOmnicanal::getIdentificador);
    }
}
