package com.marcablanca.platform.aprovisionamiento.application;

import java.util.UUID;

public record ResultadoFinalizarRegistro(UUID empresaId, String estado, String url) {
}
