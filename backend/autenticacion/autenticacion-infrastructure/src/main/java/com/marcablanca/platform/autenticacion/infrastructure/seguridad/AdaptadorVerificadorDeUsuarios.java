package com.marcablanca.platform.autenticacion.infrastructure.seguridad;

import com.marcablanca.platform.autenticacion.application.port.out.DatosDeUsuario;
import com.marcablanca.platform.autenticacion.application.port.out.VerificadorDeUsuarios;
import com.marcablanca.platform.autenticacion.domain.CredencialesInvalidasException;
import com.marcablanca.platform.autenticacion.domain.UsuarioNoDisponibleException;
import com.marcablanca.platform.usuarios.domain.Contrasena;
import com.marcablanca.platform.usuarios.domain.Correo;
import com.marcablanca.platform.usuarios.domain.Usuario;
import com.marcablanca.platform.usuarios.domain.port.out.CifradorDeContrasenas;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarios;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Unico archivo de todo el modulo autenticacion que conoce el dominio de
 * usuarios (Usuario/Correo/Contrasena/RepositorioUsuarios/CifradorDeContrasenas).
 * Traduce hacia DatosDeUsuario y hacia las excepciones propias de
 * autenticacion.domain -- ningun otro archivo de autenticacion importa nada
 * de usuarios a partir de este cambio.
 */
@Component
public class AdaptadorVerificadorDeUsuarios implements VerificadorDeUsuarios {

    private final RepositorioUsuarios repositorioUsuarios;
    private final CifradorDeContrasenas cifradorDeContrasenas;

    public AdaptadorVerificadorDeUsuarios(RepositorioUsuarios repositorioUsuarios,
                                           CifradorDeContrasenas cifradorDeContrasenas) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.cifradorDeContrasenas = cifradorDeContrasenas;
    }

    @Override
    public DatosDeUsuario verificarCredenciales(String correoTexto, String contrasenaPlano) {
        Correo correo = new Correo(correoTexto);
        Contrasena contrasena = new Contrasena(contrasenaPlano);

        Usuario usuario = repositorioUsuarios.buscarPorCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        try {
            usuario.verificarCredenciales(contrasena, cifradorDeContrasenas);
        } catch (com.marcablanca.platform.usuarios.domain.CredencialesInvalidasException e) {
            throw new CredencialesInvalidasException();
        } catch (com.marcablanca.platform.usuarios.domain.UsuarioNoDisponibleException e) {
            throw new UsuarioNoDisponibleException(e.getMessage());
        }

        return new DatosDeUsuario(usuario.getId(), usuario.getCorreo().valor());
    }

    @Override
    public Optional<DatosDeUsuario> buscarPorId(UUID usuarioId) {
        return repositorioUsuarios.buscarPorId(usuarioId)
                .map(u -> new DatosDeUsuario(u.getId(), u.getCorreo().valor()));
    }
}
