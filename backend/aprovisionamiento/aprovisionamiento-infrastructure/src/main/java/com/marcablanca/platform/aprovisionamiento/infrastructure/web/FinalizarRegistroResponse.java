package com.marcablanca.platform.aprovisionamiento.infrastructure.web;

import java.util.UUID;

public record FinalizarRegistroResponse(UUID empresaId, String estado, String url) {
}
