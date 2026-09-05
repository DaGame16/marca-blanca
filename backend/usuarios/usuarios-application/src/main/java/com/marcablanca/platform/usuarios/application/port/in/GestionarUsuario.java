package com.marcablanca.platform.usuarios.application.port.in;

import com.marcablanca.platform.usuarios.domain.Usuario;
import com.marcablanca.platform.usuarios.domain.UsuarioPerfil;

import java.util.List;
import java.util.UUID;

public interface GestionarUsuario {
    Usuario crear(String correo, String contrasenaPlano, String nombreCompleto);
    Usuario actualizar(UUID uuid, String nombreCompleto);
    void desactivar(UUID uuid);
    void activar(UUID uuid);
    Usuario consultarPorUuid(UUID uuid);
    List<Usuario> listar();

    UsuarioPerfil actualizarPerfil(UUID usuarioUuid, String cedula, String tipoDocumento,
                                    String telefono, String direccion,
                                    String contactoEmergencia, String telefonoEmergencia);
}