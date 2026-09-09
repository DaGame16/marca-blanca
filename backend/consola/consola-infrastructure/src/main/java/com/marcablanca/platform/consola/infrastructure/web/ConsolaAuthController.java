package com.marcablanca.platform.consola.infrastructure.web;

import com.marcablanca.platform.consola.application.ResultadoLoginOperador;
import com.marcablanca.platform.consola.application.port.in.AutenticarOperador;
import com.marcablanca.platform.consola.application.port.in.CambiarContrasenaDeOperador;
import com.marcablanca.platform.consola.domain.CredencialesDeOperadorInvalidasException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Autenticacion de la consola de operacion. Rutas bajo {@code /api/v1/consola/auth}
 * -- gobernadas por la cadena de seguridad propia ({@code ConfiguracionSeguridadConsola}),
 * no por la de tenant. {@code /login} es publico; {@code /cambiar-contrasena} exige
 * token (aunque sea el de contrasena temporal).
 */
@RestController
@RequestMapping("/api/v1/consola/auth")
public class ConsolaAuthController {

    private final AutenticarOperador autenticarOperador;
    private final CambiarContrasenaDeOperador cambiarContrasenaDeOperador;

    public ConsolaAuthController(AutenticarOperador autenticarOperador,
                                CambiarContrasenaDeOperador cambiarContrasenaDeOperador) {
        this.autenticarOperador = autenticarOperador;
        this.cambiarContrasenaDeOperador = cambiarContrasenaDeOperador;
    }

    @PostMapping("/login")
    public LoginOperadorResponse login(@Valid @RequestBody LoginOperadorRequest peticion) {
        ResultadoLoginOperador r = autenticarOperador.ejecutar(peticion.correo(), peticion.contrasena());
        return new LoginOperadorResponse(
                r.operadorId(), r.correo(), r.rol(), r.token(), r.debeCambiarContrasena());
    }

    @PostMapping("/cambiar-contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@Valid @RequestBody CambiarContrasenaOperadorRequest peticion) {
        cambiarContrasenaDeOperador.ejecutar(
                operadorAutenticadoId(), peticion.contrasenaActual(), peticion.contrasenaNueva());
    }

    private UUID operadorAutenticadoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UUID id) {
            return id;
        }
        throw new CredencialesDeOperadorInvalidasException();
    }
}
