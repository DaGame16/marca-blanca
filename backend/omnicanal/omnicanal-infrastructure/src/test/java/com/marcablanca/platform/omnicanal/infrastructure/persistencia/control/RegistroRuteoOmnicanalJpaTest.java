package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroRuteoOmnicanalJpaTest {

    @Mock
    EmpresaOmnicanalJpaRepository empresasOmnicanal;
    @Mock
    EmpresaRefDeOmnicanalJpaRepository empresasRef;

    @AfterEach
    void limpiar() {
        ContextoEmpresaActual.limpiar();
    }

    private RegistroRuteoOmnicanalJpa registro() {
        return new RegistroRuteoOmnicanalJpa(empresasOmnicanal, empresasRef);
    }

    private void empresaEnContexto(long id) {
        ContextoEmpresaActual.establecer("acme");
        EmpresaRefDeOmnicanal ref = org.mockito.Mockito.mock(EmpresaRefDeOmnicanal.class);
        lenient().when(ref.getId()).thenReturn(id);
        lenient().when(empresasRef.findByIdentificador("acme")).thenReturn(Optional.of(ref));
    }

    @Test
    void devuelve_el_secreto_existente_sin_crear_nada() {
        empresaEnContexto(7L);
        EmpresaOmnicanalEntity fila = org.mockito.Mockito.mock(EmpresaOmnicanalEntity.class);
        when(fila.getWebhookSecret()).thenReturn("secreto-viejo");
        when(empresasOmnicanal.findByEmpresaId(7L)).thenReturn(Optional.of(fila));

        assertEquals("secreto-viejo", registro().secretoWebhook());
        verify(empresasOmnicanal, never()).save(any());
    }

    @Test
    void crea_la_fila_la_primera_vez() {
        empresaEnContexto(7L);
        when(empresasOmnicanal.findByEmpresaId(7L)).thenReturn(Optional.empty());
        when(empresasOmnicanal.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String secreto = registro().secretoWebhook();

        assertFalse(secreto == null || secreto.isBlank());
        verify(empresasOmnicanal).save(any(EmpresaOmnicanalEntity.class));
    }

    @Test
    void rotar_guarda_un_secreto_distinto() {
        empresaEnContexto(7L);
        EmpresaOmnicanalEntity fila = new EmpresaOmnicanalEntity(7L, "secreto-viejo");
        when(empresasOmnicanal.findByEmpresaId(7L)).thenReturn(Optional.of(fila));
        when(empresasOmnicanal.save(fila)).thenReturn(fila);

        String nuevo = registro().rotarSecretoWebhook();

        assertFalse("secreto-viejo".equals(nuevo));
        assertFalse(nuevo == null || nuevo.isBlank());
        verify(empresasOmnicanal).save(fila);
    }

    @Test
    void sin_empresa_en_contexto_falla() {
        assertThrows(IllegalStateException.class, () -> registro().secretoWebhook());
    }
}
