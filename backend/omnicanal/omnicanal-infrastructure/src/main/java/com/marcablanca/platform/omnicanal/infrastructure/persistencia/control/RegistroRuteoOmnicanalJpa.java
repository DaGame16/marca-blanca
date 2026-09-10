package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.omnicanal.application.port.out.RegistroRuteoOmnicanal;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * Lee/crea/rota la fila de plataforma.tbl_empresas_omnicanal de la empresa
 * activa. Vive en persistencia.control porque necesita el mapeo minimo
 * EmpresaRefDeOmnicanal (package-private) para pasar del "identificador" del
 * ContextoEmpresaActual al empresa_id.
 */
@Component
class RegistroRuteoOmnicanalJpa implements RegistroRuteoOmnicanal {

    private final EmpresaOmnicanalJpaRepository empresasOmnicanal;
    private final EmpresaRefDeOmnicanalJpaRepository empresasRef;
    private final SecureRandom random = new SecureRandom();

    RegistroRuteoOmnicanalJpa(EmpresaOmnicanalJpaRepository empresasOmnicanal,
                              EmpresaRefDeOmnicanalJpaRepository empresasRef) {
        this.empresasOmnicanal = empresasOmnicanal;
        this.empresasRef = empresasRef;
    }

    @Override
    public String secretoWebhook() {
        return filaDeEmpresaActiva()
                .orElseGet(this::crear)
                .getWebhookSecret();
    }

    @Override
    public String rotarSecretoWebhook() {
        EmpresaOmnicanalEntity fila = filaDeEmpresaActiva().orElseGet(this::crear);
        fila.rotarSecreto(nuevoSecreto());
        return empresasOmnicanal.save(fila).getWebhookSecret();
    }

    private Optional<EmpresaOmnicanalEntity> filaDeEmpresaActiva() {
        return empresasOmnicanal.findByEmpresaId(empresaIdActiva());
    }

    private EmpresaOmnicanalEntity crear() {
        return empresasOmnicanal.save(new EmpresaOmnicanalEntity(empresaIdActiva(), nuevoSecreto()));
    }

    private Long empresaIdActiva() {
        String identificador = ContextoEmpresaActual.obtener().orElseThrow(() -> new IllegalStateException(
                "No hay empresa activa en el contexto -- endpoint de config mal protegido."));
        return empresasRef.findByIdentificador(identificador)
                .map(EmpresaRefDeOmnicanal::getId)
                .orElseThrow(() -> new IllegalStateException("La empresa " + identificador + " no existe."));
    }

    private String nuevoSecreto() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
