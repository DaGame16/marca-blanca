package com.marcablanca.platform.correo.application;

import com.marcablanca.platform.correo.application.port.in.EnviarCorreoDeBienvenida.ComandoBienvenida;
import com.marcablanca.platform.correo.application.port.out.ProveedorDeCorreo;
import com.marcablanca.platform.correo.application.port.out.RenderizadorDePlantillas;
import com.marcablanca.platform.correo.domain.DireccionCorreoInvalidaException;
import com.marcablanca.platform.correo.domain.MensajeDeCorreo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnviarCorreoDeBienvenidaServiceTest {

    @Mock
    ProveedorDeCorreo proveedorDeCorreo;
    @Mock
    RenderizadorDePlantillas renderizador;

    @Test
    void arma_los_datos_de_la_plantilla_y_envia_el_correo() {
        when(renderizador.renderizar(eq("bienvenida"), anyMap())).thenReturn("<html>bienvenido</html>");
        var servicio = new EnviarCorreoDeBienvenidaService(proveedorDeCorreo, renderizador);

        var comando = new ComandoBienvenida("Nuevo@Cliente.com", "Ana Gomez", "Empresa SAS",
                "https://empresa.marcablanca.com", "Xk9#mPq2");

        servicio.ejecutar(comando);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> datosCaptor = ArgumentCaptor.forClass(Map.class);
        verify(renderizador).renderizar(eq("bienvenida"), datosCaptor.capture());
        assertEquals("nuevo@cliente.com", datosCaptor.getValue().get("correoAcceso"));
        assertEquals("Xk9#mPq2", datosCaptor.getValue().get("contrasenaGenerada"));

        ArgumentCaptor<MensajeDeCorreo> mensajeCaptor = ArgumentCaptor.forClass(MensajeDeCorreo.class);
        verify(proveedorDeCorreo).enviar(mensajeCaptor.capture());
        assertEquals("nuevo@cliente.com", mensajeCaptor.getValue().destinatario().valor());
    }

    @Test
    void correo_destino_invalido_no_llega_a_intentar_el_envio() {
        var servicio = new EnviarCorreoDeBienvenidaService(proveedorDeCorreo, renderizador);
        var comando = new ComandoBienvenida("no-es-un-correo", "Ana Gomez", "Empresa SAS", "https://x.com", "clave");

        assertThrows(DireccionCorreoInvalidaException.class, () -> servicio.ejecutar(comando));
        verifyNoInteractions(proveedorDeCorreo);
    }
}
