package com.marcablanca.platform.autenticacion.infrastructure.web;

import com.marcablanca.platform.autenticacion.application.ResultadoAutenticacion;
import com.marcablanca.platform.autenticacion.application.port.in.AutenticarUsuario;
import com.marcablanca.platform.autenticacion.application.port.in.RenovarToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AutenticarUsuario autenticarUsuario;
    private final RenovarToken renovarToken;

    public AuthController(AutenticarUsuario autenticarUsuario, RenovarToken renovarToken) {
        this.autenticarUsuario = autenticarUsuario;
        this.renovarToken = renovarToken;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        ResultadoAutenticacion resultado = autenticarUsuario.ejecutar(request.correo(), request.contrasena());
        return ResponseEntity.ok(new LoginResponse(resultado.usuarioId(), resultado.token(), resultado.refreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        ResultadoAutenticacion resultado = renovarToken.ejecutar(request.refreshToken());
        return ResponseEntity.ok(new RefreshResponse(resultado.usuarioId(), resultado.token(), resultado.refreshToken()));
    }
}
