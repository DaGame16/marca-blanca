package com.marcablanca.platform.usuarios.infrastructure.web;

public record CrearUsuarioRequest(String correo, String contrasena, String nombreCompleto) {}