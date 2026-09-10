package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolverEmpresaPorWebhookSecretoJpaTest {

    @Mock
    EmpresaOmnicanalJpaRepository empresasOmnicanal;
    @Mock
    EmpresaRefDeOmnicanalJpaRepository empresasRef;

    private ResolverEmpresaPorWebhookSecretoJpa resolver() {
        return new ResolverEmpresaPorWebhookSecretoJpa(empresasOmnicanal, empresasRef);
    }

    @Test
    void secreto_nulo_devuelve_vacio_sin_tocar_la_base() {
        assertTrue(resolver().resolverIdentificadorEmpresa(null).isEmpty());
        verifyNoInteractions(empresasOmnicanal, empresasRef);
    }

    @Test
    void secreto_en_blanco_devuelve_vacio_sin_tocar_la_base() {
        assertTrue(resolver().resolverIdentificadorEmpresa("   ").isEmpty());
        verifyNoInteractions(empresasOmnicanal, empresasRef);
    }

    @Test
    void secreto_desconocido_devuelve_vacio() {
        when(empresasOmnicanal.findByWebhookSecret("nope")).thenReturn(Optional.empty());

        assertTrue(resolver().resolverIdentificadorEmpresa("nope").isEmpty());
        verifyNoInteractions(empresasRef);
    }

    @Test
    void config_apunta_a_una_empresa_que_no_existe_devuelve_vacio() {
        EmpresaOmnicanalEntity config = org.mockito.Mockito.mock(EmpresaOmnicanalEntity.class);
        when(config.getEmpresaId()).thenReturn(99L);
        when(empresasOmnicanal.findByWebhookSecret("s3cr3t")).thenReturn(Optional.of(config));
        when(empresasRef.findById(99L)).thenReturn(Optional.empty());

        assertTrue(resolver().resolverIdentificadorEmpresa("s3cr3t").isEmpty());
    }

    @Test
    void secreto_valido_devuelve_el_identificador_de_la_empresa() {
        EmpresaOmnicanalEntity config = org.mockito.Mockito.mock(EmpresaOmnicanalEntity.class);
        when(config.getEmpresaId()).thenReturn(7L);
        EmpresaRefDeOmnicanal empresa = org.mockito.Mockito.mock(EmpresaRefDeOmnicanal.class);
        lenient().when(empresa.getId()).thenReturn(7L);
        when(empresa.getIdentificador()).thenReturn("acme");
        when(empresasOmnicanal.findByWebhookSecret("s3cr3t")).thenReturn(Optional.of(config));
        when(empresasRef.findById(7L)).thenReturn(Optional.of(empresa));

        assertEquals(Optional.of("acme"), resolver().resolverIdentificadorEmpresa("s3cr3t"));
    }
}
