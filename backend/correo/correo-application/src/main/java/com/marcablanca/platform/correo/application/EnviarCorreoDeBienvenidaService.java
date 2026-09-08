package com.marcablanca.platform.correo.application;

import com.marcablanca.platform.correo.application.port.in.EnviarCorreoDeBienvenida;
import com.marcablanca.platform.correo.application.port.out.ProveedorDeCorreo;
import com.marcablanca.platform.correo.application.port.out.RenderizadorDePlantillas;
import com.marcablanca.platform.correo.domain.DireccionCorreo;
import com.marcablanca.platform.correo.domain.MensajeDeCorreo;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnviarCorreoDeBienvenidaService implements EnviarCorreoDeBienvenida {

    private final ProveedorDeCorreo proveedorDeCorreo;
    private final RenderizadorDePlantillas renderizador;

    public EnviarCorreoDeBienvenidaService(ProveedorDeCorreo proveedorDeCorreo, RenderizadorDePlantillas renderizador) {
        this.proveedorDeCorreo = proveedorDeCorreo;
        this.renderizador = renderizador;
    }

    @Override
    public void ejecutar(ComandoBienvenida comando) {
        DireccionCorreo destinatario = new DireccionCorreo(comando.correoDestino());

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("nombreContacto", comando.nombreContacto());
        datos.put("nombreEmpresa", comando.nombreEmpresa());
        datos.put("urlSitio", comando.urlSitio());
        datos.put("correoAcceso", destinatario.valor());
        datos.put("contrasenaGenerada", comando.contrasenaGenerada());

        String cuerpo = renderizador.renderizar("bienvenida", datos);
        proveedorDeCorreo.enviar(new MensajeDeCorreo(destinatario, "Bienvenido a tu nueva plataforma", cuerpo));
    }
}
