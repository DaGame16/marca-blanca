package com.marcablanca.platform.aprovisionamiento.application;

import java.util.UUID;

public record ResultadoRegistroEmpresa(UUID empresaId, String identificador, String dominio, String estado) {
}
