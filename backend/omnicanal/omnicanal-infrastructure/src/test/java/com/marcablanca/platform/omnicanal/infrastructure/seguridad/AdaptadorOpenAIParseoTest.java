package com.marcablanca.platform.omnicanal.infrastructure.seguridad;

import com.marcablanca.platform.omnicanal.application.port.out.RepositorioConfiguracionOmnicanal;
import com.marcablanca.platform.omnicanal.domain.Resultado;
import com.marcablanca.platform.omnicanal.domain.ResultadoAnalisisIa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class AdaptadorOpenAIParseoTest {

    @Mock
    RepositorioConfiguracionOmnicanal configuracion;

    private AdaptadorOpenAI adaptador() {
        return new AdaptadorOpenAI("api-key", "gpt-4.1-mini", configuracion, new ObjectMapper());
    }

    @Test
    void sanitiza_enums_al_valor_canonico_del_catalogo() {
        String json = """
                {
                  "resultado": "RESUELTO",
                  "motivo_contacto": "Soporte",
                  "categoria_oficina": "contratos",
                  "area_destino": "COMERCIAL",
                  "sentimiento_inicial": "Negativo",
                  "sentimiento_final": "positivo",
                  "esfuerzo_cliente": "ALTO",
                  "tipo_ultimo_mensaje_empresa": "Pidio_Datos",
                  "fcr": true,
                  "temas": ["a","b"]
                }
                """;

        ResultadoAnalisisIa r = adaptador().parsearRespuesta(json);

        assertEquals(Resultado.RESUELTO, r.resultado());
        assertEquals("soporte", r.motivoContacto());
        assertEquals("CONTRATOS", r.categoriaOficina());
        assertEquals("comercial", r.areaDestino());
        assertEquals("negativo", r.sentimientoInicial());
        assertEquals("positivo", r.sentimientoFinal());
        assertEquals("alto", r.esfuerzoCliente());
        assertEquals("pidio_datos", r.tipoUltimoMensajeEmpresa());
    }

    @Test
    void motivo_ausente_o_invalido_cae_al_default() {
        assertEquals("información", adaptador().parsearRespuesta("{}").motivoContacto());
        assertEquals("información",
                adaptador().parsearRespuesta("{\"motivo_contacto\":\"algo raro\"}").motivoContacto());
    }

    @Test
    void enum_fuera_de_catalogo_queda_null() {
        String json = """
                {"resultado":"abandonado","categoria_oficina":"XYZ","area_destino":"otra",
                 "sentimiento_inicial":"feliz","esfuerzo_cliente":"medio-alto",
                 "tipo_ultimo_mensaje_empresa":"cualquier_cosa"}
                """;
        ResultadoAnalisisIa r = adaptador().parsearRespuesta(json);
        assertNull(r.resultado());
        assertNull(r.categoriaOficina());
        assertNull(r.areaDestino());
        assertNull(r.sentimientoInicial());
        assertNull(r.esfuerzoCliente());
        assertNull(r.tipoUltimoMensajeEmpresa());
    }

    @Test
    void temas_se_corta_a_seis() {
        String json = "{\"temas\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\"]}";
        assertEquals(6, adaptador().parsearRespuesta(json).temas().size());
    }

    @Test
    void sanitizar_enum_null_vacio_y_desconocido() {
        List<String> cat = List.of("uno", "Dos");
        assertNull(AdaptadorOpenAI.sanitizarEnum(null, cat));
        assertNull(AdaptadorOpenAI.sanitizarEnum("   ", cat));
        assertNull(AdaptadorOpenAI.sanitizarEnum("tres", cat));
        assertEquals("Dos", AdaptadorOpenAI.sanitizarEnum("  DOS ", cat));
    }
}
