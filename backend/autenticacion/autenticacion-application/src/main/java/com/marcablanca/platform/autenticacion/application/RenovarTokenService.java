package com.marcablanca.platform.autenticacion.application;

import com.marcablanca.platform.autenticacion.application.port.in.RenovarToken;
import com.marcablanca.platform.autenticacion.application.port.out.AlmacenDeTokensDeRefresco;
import com.marcablanca.platform.autenticacion.application.port.out.GeneradorDeToken;
import com.marcablanca.platform.autenticacion.domain.TokenDeRefrescoInvalidoException;
import com.marcablanca.platform.usuarios.domain.Usuario;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarios;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RenovarTokenService implements RenovarToken {

    private static final long REFRESCO_DIAS_VALIDEZ = 7;

    private final AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco;
    private final GeneradorDeToken generadorDeToken;
    private final RepositorioUsuarios repositorioUsuarios;

    public RenovarTokenService(AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco,
                                GeneradorDeToken generadorDeToken,
                                RepositorioUsuarios repositorioUsuarios) {
        this.almacenDeTokensDeRefresco = almacenDeTokensDeRefresco;
        this.generadorDeToken = generadorDeToken;
        this.repositorioUsuarios = repositorioUsuarios;
    }

    @Override
    public ResultadoAutenticacion ejecutar(String refreshTokenActual) {
        String hashActual = GeneradorTokenDeRefresco.hashear(refreshTokenActual);

        UUID usuarioId = almacenDeTokensDeRefresco.buscarUsuarioPorHashActivo(hashActual, OffsetDateTime.now())
                .orElseThrow(TokenDeRefrescoInvalidoException::new);

        Usuario usuario = repositorioUsuarios.buscarPorId(usuarioId)
                .orElseThrow(TokenDeRefrescoInvalidoException::new);

        // Rotacion: el token usado se invalida antes de emitir uno nuevo. Si
        // alguien vuelve a usar este mismo valor despues, ya no se va a
        // encontrar activo -- señal de que el token estaba comprometido.
        almacenDeTokensDeRefresco.eliminarPorHash(hashActual);

        String nuevoToken = generadorDeToken.generarPara(usuario);

        String nuevoRefrescoValor = GeneradorTokenDeRefresco.generarValor();
        String nuevoRefrescoHash = GeneradorTokenDeRefresco.hashear(nuevoRefrescoValor);
        OffsetDateTime expiraEn = OffsetDateTime.now().plusDays(REFRESCO_DIAS_VALIDEZ);
        almacenDeTokensDeRefresco.guardar(usuario.getId(), nuevoRefrescoHash, expiraEn, null);

        return new ResultadoAutenticacion(usuario.getId(), nuevoToken, nuevoRefrescoValor);
    }
}
