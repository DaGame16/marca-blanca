package com.marcablanca.platform.correo.application.port.out;

import java.util.Map;

public interface RenderizadorDePlantillas {
    String renderizar(String nombrePlantilla, Map<String, Object> datos);
}
