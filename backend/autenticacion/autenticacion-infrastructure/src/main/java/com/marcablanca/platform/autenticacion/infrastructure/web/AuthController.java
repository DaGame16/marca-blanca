package com.marcablanca.platform.autenticacion.infrastructure.web;

import com.marcablanca.platform.autenticacion.application.ResultadoAutenticacion;
import com.marcablanca.platform.autenticacion.application.port.in.AutenticarUsuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AutenticarUsuario autenticarUsuario;

    public AuthController(AutenticarUsuario autenticarUsuario) {
        this.autenticarUsuario = autenticarUsuario;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        ResultadoAutenticacion resultado = autenticarUsuario.ejecutar(request.correo(), request.contrasena());
        return ResponseEntity.ok(new LoginResponse(resultado.usuarioId(), resultado.token()));
    }
}
