package com.marcablanca.platform.correo.infrastructure;

import com.marcablanca.platform.correo.application.port.out.RenderizadorDePlantillas;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Reemplazo simple de {{clave}} -- nada de motor de plantillas pesado para
 * 2 correos. El HTML vive en archivos aparte (src/main/resources/plantillas/
 * correo/), no como texto embebido en este archivo, para que el copy se
 * pueda editar sin tocar logica.
 */
@Component
public class AdaptadorRenderizadorDePlantillas implements RenderizadorDePlantillas {

    @Override
    public String renderizar(String nombrePlantilla, Map<String, Object> datos) {
        String resultado = cargarPlantilla(nombrePlantilla);
        for (Map.Entry<String, Object> entrada : datos.entrySet()) {
            String valor = entrada.getValue() == null ? "" : entrada.getValue().toString();
            resultado = resultado.replace("{{" + entrada.getKey() + "}}", valor);
        }
        return resultado;
    }

    private String cargarPlantilla(String nombre) {
        try {
            ClassPathResource recurso = new ClassPathResource("plantillas/correo/" + nombre + ".html");
            return new String(recurso.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("No se encontro la plantilla de correo: " + nombre, e);
        }
    }
}
