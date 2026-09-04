package com.marcablanca.platform.autenticacion.application;

import com.marcablanca.platform.autenticacion.application.port.in.AutenticarUsuario;
import com.marcablanca.platform.autenticacion.application.port.out.AlmacenDeTokensDeRefresco;
import com.marcablanca.platform.autenticacion.application.port.out.GeneradorDeToken;
import com.marcablanca.platform.usuarios.domain.Contrasena;
import com.marcablanca.platform.usuarios.domain.Correo;
import com.marcablanca.platform.usuarios.domain.CredencialesInvalidasException;
import com.marcablanca.platform.usuarios.domain.Usuario;
import com.marcablanca.platform.usuarios.domain.port.out.CifradorDeContrasenas;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarios;

import java.time.OffsetDateTime;

/** Orquesta el login. No decide reglas de negocio: eso vive en Usuario.verificarCredenciales(). */
public class AutenticarUsuarioService implements AutenticarUsuario {

    private static final long REFRESCO_DIAS_VALIDEZ = 7;

    private final RepositorioUsuarios repositorioUsuarios;
    private final CifradorDeContrasenas cifradorDeContrasenas;
    private final GeneradorDeToken generadorDeToken;
    private final AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco;

    public AutenticarUsuarioService(RepositorioUsuarios repositorioUsuarios,
                                     CifradorDeContrasenas cifradorDeContrasenas,
                                     GeneradorDeToken generadorDeToken,
                                     AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.cifradorDeContrasenas = cifradorDeContrasenas;
        this.generadorDeToken = generadorDeToken;
        this.almacenDeTokensDeRefresco = almacenDeTokensDeRefresco;
    }

    @Override
    public ResultadoAutenticacion ejecutar(String correoTexto, String contrasenaPlano) {
        Correo correo = new Correo(correoTexto);
        Contrasena contrasena = new Contrasena(contrasenaPlano);

        Usuario usuario = repositorioUsuarios.buscarPorCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        usuario.verificarCredenciales(contrasena, cifradorDeContrasenas);

        String token = generadorDeToken.generarPara(usuario);

        String refrescoValor = GeneradorTokenDeRefresco.generarValor();
        String refrescoHash = GeneradorTokenDeRefresco.hashear(refrescoValor);
        OffsetDateTime expiraEn = OffsetDateTime.now().plusDays(REFRESCO_DIAS_VALIDEZ);
        // infoDispositivo en null por ahora -- columna nullable, se puede sumar despues.
        almacenDeTokensDeRefresco.guardar(usuario.getId(), refrescoHash, expiraEn, null);

        return new ResultadoAutenticacion(usuario.getId(), token, refrescoValor);
    }
}
