package com.marcablanca.platform.autenticacion.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto propio de autenticacion para consultar usuarios -- nunca se importa
 * Usuario/Correo/Contrasena/RepositorioUsuarios de usuarios-domain fuera de
 * la implementacion de este puerto (AdaptadorVerificadorDeUsuarios).
 */
public interface VerificadorDeUsuarios {

    /** Lanza CredencialesInvalidasException o UsuarioNoDisponibleException (propias) si no corresponde. */
    DatosDeUsuario verificarCredenciales(String correo, String contrasenaPlano);

    Optional<DatosDeUsuario> buscarPorId(UUID usuarioId);
}
