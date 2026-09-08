package com.marcablanca.platform.correo.infrastructure;

import com.marcablanca.platform.correo.application.EnviarCorreoDeBienvenidaService;
import com.marcablanca.platform.correo.application.EnviarCorreoDeResumenDePagoService;
import com.marcablanca.platform.correo.application.port.in.EnviarCorreoDeBienvenida;
import com.marcablanca.platform.correo.application.port.in.EnviarCorreoDeResumenDePago;
import com.marcablanca.platform.correo.application.port.out.ProveedorDeCorreo;
import com.marcablanca.platform.correo.application.port.out.RenderizadorDePlantillas;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los servicios de aplicacion no tienen anotaciones de Spring a proposito
 * -- esta clase los conecta como beans, mismo patron que el resto del proyecto.
 */
@Configuration
public class ConfiguracionCorreo {

    @Bean
    public EnviarCorreoDeResumenDePago enviarCorreoDeResumenDePago(ProveedorDeCorreo proveedorDeCorreo,
                                                                     RenderizadorDePlantillas renderizador) {
        return new EnviarCorreoDeResumenDePagoService(proveedorDeCorreo, renderizador);
    }

    @Bean
    public EnviarCorreoDeBienvenida enviarCorreoDeBienvenida(ProveedorDeCorreo proveedorDeCorreo,
                                                               RenderizadorDePlantillas renderizador) {
        return new EnviarCorreoDeBienvenidaService(proveedorDeCorreo, renderizador);
    }
}
