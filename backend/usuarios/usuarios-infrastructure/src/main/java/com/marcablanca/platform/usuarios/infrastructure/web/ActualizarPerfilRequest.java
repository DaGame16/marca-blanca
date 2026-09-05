package com.marcablanca.platform.usuarios.infrastructure.web;

public record ActualizarPerfilRequest(String cedula, String tipoDocumento, String telefono,
                                        String direccion, String contactoEmergencia,
                                        String telefonoEmergencia) {}