package com.marcablanca.platform.omnicanal.infrastructure.persistencia.control;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.modulosempresa.application.port.in.ListarModulosDeEmpresa;
import com.marcablanca.platform.modulosempresa.domain.ModuloDeEmpresa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PuenteModulosOmnicanalTest {

    private static final UUID EMPRESA_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    EmpresaRefDeOmnicanalJpaRepository empresasRef;
    @Mock
    ListarModulosDeEmpresa listarModulosDeEmpresa;

    @AfterEach
    void limpiarContexto() {
        ContextoEmpresaActual.limpiar();
    }

    private PuenteModulosOmnicanal puente() {
        return new PuenteModulosOmnicanal(empresasRef, listarModulosDeEmpresa);
    }

    private void empresaEnContexto(String slug) {
        ContextoEmpresaActual.establecer(slug);
        EmpresaRefDeOmnicanal ref = org.mockito.Mockito.mock(EmpresaRefDeOmnicanal.class);
        lenient().when(ref.getUuid()).thenReturn(EMPRESA_UUID);
        lenient().when(empresasRef.findByIdentificador(slug)).thenReturn(Optional.of(ref));
    }

    @Test
    void sin_empresa_en_contexto_devuelve_false() {
        assertFalse(puente().paraEmpresaActual());
        verifyNoInteractions(listarModulosDeEmpresa);
    }

    @Test
    void empresa_desconocida_devuelve_false() {
        ContextoEmpresaActual.establecer("fantasma");
        when(empresasRef.findByIdentificador("fantasma")).thenReturn(Optional.empty());

        assertFalse(puente().paraEmpresaActual());
        verifyNoInteractions(listarModulosDeEmpresa);
    }

    @Test
    void modulo_omnicanal_activo_devuelve_true() {
        empresaEnContexto("acme");
        when(listarModulosDeEmpresa.ejecutar(EMPRESA_UUID)).thenReturn(List.of(
                new ModuloDeEmpresa("usuarios", "Usuarios", "", true),
                new ModuloDeEmpresa("omnicanal", "Comunicacion omnicanal", "", true)));

        assertTrue(puente().paraEmpresaActual());
    }

    @Test
    void modulo_omnicanal_presente_pero_inactivo_devuelve_false() {
        empresaEnContexto("acme");
        when(listarModulosDeEmpresa.ejecutar(EMPRESA_UUID)).thenReturn(List.of(
                new ModuloDeEmpresa("omnicanal", "Comunicacion omnicanal", "", false)));

        assertFalse(puente().paraEmpresaActual());
    }

    @Test
    void modulo_omnicanal_ausente_devuelve_false() {
        empresaEnContexto("acme");
        when(listarModulosDeEmpresa.ejecutar(EMPRESA_UUID)).thenReturn(List.of(
                new ModuloDeEmpresa("usuarios", "Usuarios", "", true)));

        assertFalse(puente().paraEmpresaActual());
    }
}
