package com.marcablanca.platform.autenticacion.application;

import com.marcablanca.platform.autenticacion.application.port.in.RenovarToken;
import com.marcablanca.platform.autenticacion.application.port.out.AlmacenDeTokensDeRefresco;
import com.marcablanca.platform.autenticacion.application.port.out.ConsultarPermisosDeUsuario;
import com.marcablanca.platform.autenticacion.application.port.out.DatosDeUsuario;
import com.marcablanca.platform.autenticacion.application.port.out.GeneradorDeToken;
import com.marcablanca.platform.autenticacion.application.port.out.VerificadorDeUsuarios;
import com.marcablanca.platform.autenticacion.domain.TokenDeRefrescoInvalidoException;
import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

public class RenovarTokenService implements RenovarToken {

    private static final long REFRESCO_DIAS_VALIDEZ = 7;

    private final AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco;
    private final GeneradorDeToken generadorDeToken;
    private final VerificadorDeUsuarios verificadorDeUsuarios;
    private final ConsultarPermisosDeUsuario consultarPermisosDeUsuario;

    public RenovarTokenService(AlmacenDeTokensDeRefresco almacenDeTokensDeRefresco,
                                GeneradorDeToken generadorDeToken,
                                VerificadorDeUsuarios verificadorDeUsuarios,
                                ConsultarPermisosDeUsuario consultarPermisosDeUsuario) {
        this.almacenDeTokensDeRefresco = almacenDeTokensDeRefresco;
        this.generadorDeToken = generadorDeToken;
        this.verificadorDeUsuarios = verificadorDeUsuarios;
        this.consultarPermisosDeUsuario = consultarPermisosDeUsuario;
    }

    @Override
    public ResultadoAutenticacion ejecutar(String refreshTokenActual) {
        String hashActual = GeneradorTokenDeRefresco.hashear(refreshTokenActual);

        UUID usuarioId = almacenDeTokensDeRefresco.buscarUsuarioPorHashActivo(hashActual, OffsetDateTime.now(ZoneOffset.UTC))
                .orElseThrow(TokenDeRefrescoInvalidoException::new);

        DatosDeUsuario usuario = verificadorDeUsuarios.buscarPorId(usuarioId)
                .orElseThrow(TokenDeRefrescoInvalidoException::new);

        // Rotacion: el token usado se invalida antes de emitir uno nuevo. Si
        // alguien vuelve a usar este mismo valor despues, ya no se va a
        // encontrar activo -- señal de que el token estaba comprometido.
        almacenDeTokensDeRefresco.eliminarPorHash(hashActual);

        String identificadorEmpresa = ContextoEmpresaActual.obtener()
                .orElseThrow(() -> new IllegalStateException("No hay empresa activa en el contexto de la peticion"));

        Set<String> permisos = consultarPermisosDeUsuario.permisosDe(usuario.id());
        String nuevoToken = generadorDeToken.generarPara(usuario, permisos, identificadorEmpresa);

        String nuevoRefrescoValor = GeneradorTokenDeRefresco.generarValor();
        String nuevoRefrescoHash = GeneradorTokenDeRefresco.hashear(nuevoRefrescoValor);
        OffsetDateTime expiraEn = OffsetDateTime.now(ZoneOffset.UTC).plusDays(REFRESCO_DIAS_VALIDEZ);
        almacenDeTokensDeRefresco.guardar(usuario.id(), nuevoRefrescoHash, expiraEn, null);

        return new ResultadoAutenticacion(usuario.id(), nuevoToken, nuevoRefrescoValor,
                usuario.debeCambiarContrasena());
    }
}
