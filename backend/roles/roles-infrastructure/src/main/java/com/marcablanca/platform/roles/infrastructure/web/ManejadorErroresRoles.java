package com.marcablanca.platform.roles.infrastructure.web;

import com.marcablanca.platform.roles.domain.PermisoNoEncontradoException;
import com.marcablanca.platform.roles.domain.RolConUsuariosAsignadosException;
import com.marcablanca.platform.roles.domain.RolDelSistemaException;
import com.marcablanca.platform.roles.domain.RolNoEncontradoException;
import com.marcablanca.platform.roles.domain.RolYaExisteException;
import com.marcablanca.platform.roles.domain.UsuarioNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.marcablanca.platform.roles.infrastructure.web")
public class ManejadorErroresRoles {

    @ExceptionHandler({RolNoEncontradoException.class, PermisoNoEncontradoException.class,
            UsuarioNoEncontradoException.class})
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(RuntimeException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({RolYaExisteException.class, RolConUsuariosAsignadosException.class})
    public ResponseEntity<Map<String, Object>> manejarConflicto(RuntimeException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(RolDelSistemaException.class)
    public ResponseEntity<Map<String, Object>> manejarRolDelSistema(RolDelSistemaException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(
            HttpStatus estado, String mensaje, HttpServletRequest request) {
        Map<String, Object> cuerpo = Map.of(
                "codigo", estado.value(),
                "mensaje", mensaje,
                "instante", Instant.now().toString(),
                "ruta", request.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
