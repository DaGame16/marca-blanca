package com.marcablanca.platform.autenticacion.application.port.out;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AlmacenDeTokensDeRefresco {

    void guardar(UUID usuarioId, String hashTokenRefresco, OffsetDateTime expiraEn, String infoDispositivo);

    Optional<UUID> buscarUsuarioPorHashActivo(String hashTokenRefresco, OffsetDateTime ahora);

    void eliminarPorHash(String hashTokenRefresco);

    void eliminarTodosDeUsuario(UUID usuarioId);
}
