package com.marcablanca.platform.autenticacion.infrastructure.web;

import com.marcablanca.platform.autenticacion.application.ResultadoAutenticacion;
import com.marcablanca.platform.autenticacion.application.port.in.AutenticarUsuario;
import com.marcablanca.platform.autenticacion.application.port.in.RenovarToken;
import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Establece ContextoEmpresaActual antes de cada operacion y lo limpia
 * despues (finally), sea cual sea el resultado -- sin esto, cualquier
 * consulta contra la base de la empresa (tbl_usuarios, tbl_sesiones)
 * falla con "No hay empresa activa en el contexto de la peticion", porque
 * el enrutador multi-tenant no tiene de donde leerla.
 *
 * Se limpia en un finally por la misma razon que se documenta en
 * ContextoEmpresaActual: el servidor reutiliza threads entre peticiones --
 * si no se limpia, un request puede terminar usando la empresa del
 * request anterior en ese mismo thread.
 */
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
        ContextoEmpresaActual.establecer(request.identificadorEmpresa());
        try {
            ResultadoAutenticacion resultado = autenticarUsuario.ejecutar(request.correo(), request.contrasena());
            return ResponseEntity.ok(new LoginResponse(resultado.usuarioId(), resultado.token(), resultado.refreshToken()));
        } finally {
            ContextoEmpresaActual.limpiar();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        ContextoEmpresaActual.establecer(request.identificadorEmpresa());
        try {
            ResultadoAutenticacion resultado = renovarToken.ejecutar(request.refreshToken());
            return ResponseEntity.ok(new RefreshResponse(resultado.usuarioId(), resultado.token(), resultado.refreshToken()));
        } finally {
            ContextoEmpresaActual.limpiar();
        }
    }
}
