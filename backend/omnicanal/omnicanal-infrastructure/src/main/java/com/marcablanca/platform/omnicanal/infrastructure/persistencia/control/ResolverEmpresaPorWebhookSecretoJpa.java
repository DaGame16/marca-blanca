package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import com.marcablanca.platform.omnicanal.application.port.out.ResolverEmpresaPorWebhookSecreto;

import java.util.Optional;

/**
 * PENDIENTE -- implementacion real, lista para cuando exista
 * tbl_empresas_omnicanal. NO lleva @Component a proposito -- ver nota en
 * EmpresaOmnicanalEntity para los pasos de activacion completos.
 */
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
        return empresasOmnicanal.findByWebhookSecret(webhookSecret)
                .flatMap(config -> empresasRef.findById(config.getEmpresaId()))
                .map(EmpresaRefDeOmnicanal::getIdentificador);
    }

    @Override
    public Optional<String> resolverLiwaApiToken(String identificadorEmpresa) {
        return empresasRef.findAll().stream()
                .filter(ref -> identificadorEmpresa.equals(ref.getIdentificador()))
                .findFirst()
                .flatMap(ref -> empresasOmnicanal.findAll().stream()
                        .filter(c -> c.getEmpresaId().equals(ref.getId()))
                        .findFirst())
                .map(EmpresaOmnicanalEntity::getLiwaApiToken);
    }
}
