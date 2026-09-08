package com.marcablanca.platform.correo.application;

import com.marcablanca.platform.correo.application.port.in.EnviarCorreoDeResumenDePago.ComandoResumenDePago;
import com.marcablanca.platform.correo.application.port.in.EnviarCorreoDeResumenDePago.ComandoResumenDePago.LineaFactura;
import com.marcablanca.platform.correo.application.port.out.ProveedorDeCorreo;
import com.marcablanca.platform.correo.application.port.out.RenderizadorDePlantillas;
import com.marcablanca.platform.correo.domain.DireccionCorreoInvalidaException;
import com.marcablanca.platform.correo.domain.MensajeDeCorreo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnviarCorreoDeResumenDePagoServiceTest {

    @Mock
    ProveedorDeCorreo proveedorDeCorreo;
    @Mock
    RenderizadorDePlantillas renderizador;

    @Test
    void arma_los_datos_de_la_plantilla_y_envia_el_correo() {
        when(renderizador.renderizar(eq("resumen-pago"), anyMap())).thenReturn("<html>ok</html>");
        var servicio = new EnviarCorreoDeResumenDePagoService(proveedorDeCorreo, renderizador);

        var comando = new ComandoResumenDePago(
                "cliente@empresa.com", "Juan Perez", "Empresa SAS", "F-0001",
                List.of(new LineaFactura("Omnicanal", new BigDecimal("50000"))),
                new BigDecimal("50000"));

        servicio.ejecutar(comando);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> datosCaptor = ArgumentCaptor.forClass(Map.class);
        verify(renderizador).renderizar(eq("resumen-pago"), datosCaptor.capture());
        assertEquals("Juan Perez", datosCaptor.getValue().get("nombreContacto"));
        assertTrue(datosCaptor.getValue().get("filasLineas").toString().contains("Omnicanal"));

        ArgumentCaptor<MensajeDeCorreo> mensajeCaptor = ArgumentCaptor.forClass(MensajeDeCorreo.class);
        verify(proveedorDeCorreo).enviar(mensajeCaptor.capture());
        assertEquals("cliente@empresa.com", mensajeCaptor.getValue().destinatario().valor());
        assertEquals("<html>ok</html>", mensajeCaptor.getValue().cuerpoHtml());
    }

    @Test
    void correo_destino_invalido_no_llega_a_intentar_el_envio() {
        var servicio = new EnviarCorreoDeResumenDePagoService(proveedorDeCorreo, renderizador);
        var comando = new ComandoResumenDePago(
                "no-es-un-correo", "Juan Perez", "Empresa SAS", "F-0001", List.of(), BigDecimal.ZERO);

        assertThrows(DireccionCorreoInvalidaException.class, () -> servicio.ejecutar(comando));
        verifyNoInteractions(proveedorDeCorreo);
    }
}
