package com.marcablanca.platform.correo.application.port.in;

import java.math.BigDecimal;
import java.util.List;

public interface EnviarCorreoDeResumenDePago {

    void ejecutar(ComandoResumenDePago comando);

    record ComandoResumenDePago(String correoDestino, String nombreContacto, String nombreEmpresa,
                                 String numeroFactura, List<LineaFactura> lineas, BigDecimal total) {

        public record LineaFactura(String nombreModulo, BigDecimal valorMensual) {
        }
    }
}
