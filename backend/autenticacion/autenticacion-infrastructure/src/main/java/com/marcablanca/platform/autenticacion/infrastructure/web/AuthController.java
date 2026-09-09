package com.marcablanca.platform.autenticacion.infrastructure.web;

import com.marcablanca.platform.autenticacion.application.ResultadoAutenticacion;
import com.marcablanca.platform.autenticacion.application.port.in.AutenticarUsuario;
import com.marcablanca.platform.autenticacion.application.port.in.RenovarToken;
import com.marcablanca.platform.autenticacion.application.port.out.VerificadorDeUsuarios;
import com.marcablanca.platform.autenticacion.domain.CredencialesInvalidasException;
import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.empresas.application.port.in.ResolverEmpresaPorCorreo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Establece ContextoEmpresaActual antes de login/refresh y lo limpia despues
 * (finally). En cambiar-contrasena no hace falta: JwtAuthFilter ya lo dejo puesto
 * a partir del token.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AutenticarUsuario autenticarUsuario;
    private final RenovarToken renovarToken;
    private final VerificadorDeUsuarios verificadorDeUsuarios;
    private final ResolverEmpresaPorCorreo resolverEmpresaPorCorreo;

    public AuthController(AutenticarUsuario autenticarUsuario, RenovarToken renovarToken,
                          VerificadorDeUsuarios verificadorDeUsuarios,
                          ResolverEmpresaPorCorreo resolverEmpresaPorCorreo) {
        this.autenticarUsuario = autenticarUsuario;
        this.renovarToken = renovarToken;
        this.verificadorDeUsuarios = verificadorDeUsuarios;
        this.resolverEmpresaPorCorreo = resolverEmpresaPorCorreo;
    }

    /**
     * Para que el login solo pida correo y contrasena: el frontend llama esto
     * primero para averiguar a que empresa pertenece el correo, y con eso arma
     * la llamada a /login. Publico (bajo /api/v1/auth/**), como el resto del
     * login -- ver ResolverEmpresaPorCorreoJdbc (modulo empresas) para las
     * limitaciones de como se resuelve.
     */
    @GetMapping("/identificador-empresa")
    public ResponseEntity<IdentificadorEmpresaResponse> identificadorEmpresa(@RequestParam String correo) {
        return resolverEmpresaPorCorreo.ejecutar(correo)
                .map(identificador -> ResponseEntity.ok(new IdentificadorEmpresaResponse(identificador)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        ContextoEmpresaActual.establecer(request.identificadorEmpresa());
        try {
            ResultadoAutenticacion r = autenticarUsuario.ejecutar(request.correo(), request.contrasena());
            return ResponseEntity.ok(new LoginResponse(
                    r.usuarioId(), r.token(), r.refreshToken(), r.debeCambiarContrasena()));
        } finally {
            ContextoEmpresaActual.limpiar();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        ContextoEmpresaActual.establecer(request.identificadorEmpresa());
        try {
            ResultadoAutenticacion r = renovarToken.ejecutar(request.refreshToken());
            return ResponseEntity.ok(new RefreshResponse(
                    r.usuarioId(), r.token(), r.refreshToken(), r.debeCambiarContrasena()));
        } finally {
            ContextoEmpresaActual.limpiar();
        }
    }

    @PostMapping("/cambiar-contrasena")
    public ResponseEntity<Void> cambiarContrasena(@RequestBody CambiarContrasenaRequest body) {
        verificadorDeUsuarios.cambiarContrasena(
                usuarioAutenticadoId(), body.contrasenaActual(), body.contrasenaNueva());
        return ResponseEntity.noContent().build();
    }

    private UUID usuarioAutenticadoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UUID id) {
            return id;
        }
        throw new CredencialesInvalidasException();
    }
}
