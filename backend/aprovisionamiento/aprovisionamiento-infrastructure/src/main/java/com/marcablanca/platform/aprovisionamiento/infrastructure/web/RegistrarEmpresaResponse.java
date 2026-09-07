package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

import java.util.UUID;

public record RegistrarEmpresaResponse(UUID empresaId, String estado) {
}