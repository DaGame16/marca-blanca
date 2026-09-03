package com.marcablanca.platform.empresas.domain;

import java.util.UUID;

public record Empresa(Long id, UUID uuid, String identificador, String nombreLegal, String estado) {
}
