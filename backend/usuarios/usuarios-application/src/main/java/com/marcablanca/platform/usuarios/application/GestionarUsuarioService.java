package com.marcablanca.platform.usuarios.application;

import com.marcablanca.platform.usuarios.application.port.in.GestionarUsuario;
import com.marcablanca.platform.usuarios.domain.*;
import com.marcablanca.platform.usuarios.domain.port.out.CifradorDeContrasenas;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarioPerfiles;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarios;

import java.util.List;
import java.util.UUID;

public class GestionarUsuarioService implements GestionarUsuario {

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioUsuarioPerfiles repositorioPerfiles;
    private final CifradorDeContrasenas cifrador;

    public GestionarUsuarioService(RepositorioUsuarios repositorioUsuarios,
                                    RepositorioUsuarioPerfiles repositorioPerfiles,
                                    CifradorDeContrasenas cifrador) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioPerfiles = repositorioPerfiles;
        this.cifrador = cifrador;
    }

    @Override
    public Usuario crear(String correoTexto, String contrasenaPlano, String nombreCompleto) {
        Correo correo = new Correo(correoTexto);
        if (repositorioUsuarios.buscarPorCorreo(correo).isPresent()) {
            throw new CorreoYaRegistradoException(correoTexto);
        }
        HashContrasena hash = cifrador.cifrar(new Contrasena(contrasenaPlano));
        Usuario usuario = Usuario.nuevo(correo, hash, nombreCompleto);
        return repositorioUsuarios.guardar(usuario);
    }

    @Override
    public Usuario actualizar(UUID uuid, String nombreCompleto) {
        Usuario usuario = obtenerOLanzar(uuid);
        usuario.actualizarDatos(nombreCompleto);
        return repositorioUsuarios.guardar(usuario);
    }

    @Override
    public void desactivar(UUID uuid) {
        Usuario usuario = obtenerOLanzar(uuid);
        usuario.desactivar();
        repositorioUsuarios.guardar(usuario);
    }

    @Override
    public void activar(UUID uuid) {
        Usuario usuario = obtenerOLanzar(uuid);
        usuario.activar();
        repositorioUsuarios.guardar(usuario);
    }

    @Override
    public Usuario consultarPorUuid(UUID uuid) {
        return obtenerOLanzar(uuid);
    }

    @Override
    public List<Usuario> listar() {
        return repositorioUsuarios.listarTodos();
    }

    @Override
    public UsuarioPerfil actualizarPerfil(UUID usuarioUuid, String cedula, String tipoDocumento,
                                           String telefono, String direccion,
                                           String contactoEmergencia, String telefonoEmergencia) {
        Usuario usuario = obtenerOLanzar(usuarioUuid);
        UsuarioPerfil perfil = repositorioPerfiles.buscarPorUsuarioId(usuario.getId())
                .orElseGet(() -> UsuarioPerfil.nuevo(usuario.getId()));
        perfil.actualizarDatosPersonales(cedula, tipoDocumento, null, telefono, direccion,
                contactoEmergencia, telefonoEmergencia);
        return repositorioPerfiles.guardar(perfil);
    }

    private Usuario obtenerOLanzar(UUID uuid) {
        return repositorioUsuarios.buscarPorUuid(uuid)
                .orElseThrow(() -> new UsuarioNoEncontradoException(uuid));
    }
}