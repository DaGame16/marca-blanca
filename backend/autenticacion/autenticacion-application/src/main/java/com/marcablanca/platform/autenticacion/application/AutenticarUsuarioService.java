package com.marcablanca.platform.autenticacion.application;

import com.marcablanca.platform.autenticacion.application.port.in.AutenticarUsuario;
import com.marcablanca.platform.autenticacion.application.port.out.AlmacenDeTokensDeRefresco;
import com.marcablanca.platform.autenticacion.application.port.out.DatosDeUsuario;
import com.marcablanca.platform.autenticacion.application.port.out.GeneradorDeToken;
import com.marcablanca.platform.autenticacion.application.port.out.VerificadorDeUsuarios;
import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;

import java.time.OffsetDateTime;

/**
 * Orquesta el login. La validacion de credenciales vive del otro lado de
 * VerificadorDeUsuarios (implementada por AdaptadorVerificadorDeUsuarios,
 * en infrastructure) -- este servicio ya no conoce Usuario/Correo/Contrasena.
 * Lee ContextoEmpresaActual (ya establecido por AuthController antes de
 * llamar aca) para saber que empresa embeber en el token nuevo.
 */
public class AutenticarUsuarioService implements AutenticarUsuario {

    private static final long REFRESCO_DIAS_VALIDEZ = 7;

    private final VerificadorDeUsuarios verificadorDeUsuarios;
    private final GeneradorDeToken generadorDeToken;
    private final AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco;

    public AutenticarUsuarioService(VerificadorDeUsuarios verificadorDeUsuarios,
                                     GeneradorDeToken generadorDeToken,
                                     AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco) {
        this.verificadorDeUsuarios = verificadorDeUsuarios;
        this.generadorDeToken = generadorDeToken;
        this.almacenDeTokensDeRefresco = almacenDeTokensDeRefresco;
    }

    @Override
    public ResultadoAutenticacion ejecutar(String correoTexto, String contrasenaPlano) {
        DatosDeUsuario usuario = verificadorDeUsuarios.verificarCredenciales(correoTexto, contrasenaPlano);

        String identificadorEmpresa = ContextoEmpresaActual.obtener()
                .orElseThrow(() -> new IllegalStateException("No hay empresa activa en el contexto de la peticion"));

        String token = generadorDeToken.generarPara(usuario, identificadorEmpresa);

        String refrescoValor = GeneradorTokenDeRefresco.generarValor();
        String refrescoHash = GeneradorTokenDeRefresco.hashear(refrescoValor);
        OffsetDateTime expiraEn = OffsetDateTime.now().plusDays(REFRESCO_DIAS_VALIDEZ);
        almacenDeTokensDeRefresco.guardar(usuario.id(), refrescoHash, expiraEn, null);

        return new ResultadoAutenticacion(usuario.id(), token, refrescoValor);
    }
}
