package com.marcablanca.platform.empresas.domain;

import java.util.UUID;

public record Modulo(UUID id, String codigo, String nombre, String descripcion) {
}
