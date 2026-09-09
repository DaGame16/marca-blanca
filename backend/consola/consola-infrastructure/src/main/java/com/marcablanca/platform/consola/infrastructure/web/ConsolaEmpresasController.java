package com.marcablanca.platform.consola.infrastructure.web;

import com.marcablanca.platform.consola.application.DatosEmpresaConsola;
import com.marcablanca.platform.consola.application.DetalleEmpresaConsola;
import com.marcablanca.platform.consola.application.EmpresaParaConsola;
import com.marcablanca.platform.consola.application.MarcaConsola;
import com.marcablanca.platform.consola.application.port.in.AdministrarEmpresas;
import com.marcablanca.platform.consola.domain.CredencialesDeOperadorInvalidasException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Administracion de empresas desde la consola. Gobernada por la cadena de
 * seguridad de consola: exige un token de operador valido (cualquier rol).
 */
@RestController
@RequestMapping("/api/v1/consola/empresas")
public class ConsolaEmpresasController {

    private final AdministrarEmpresas administrarEmpresas;

    public ConsolaEmpresasController(AdministrarEmpresas administrarEmpresas) {
        this.administrarEmpresas = administrarEmpresas;
    }

    @GetMapping
    public List<EmpresaConsolaResponse> listar() {
        return administrarEmpresas.listar().stream().map(ConsolaEmpresasController::aRespuesta).toList();
    }

    @GetMapping("/{empresaId}")
    public ResponseEntity<DetalleEmpresaConsola> detalle(@PathVariable UUID empresaId) {
        return administrarEmpresas.detalle(empresaId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{empresaId}/suspender")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspender(@PathVariable UUID empresaId) {
        administrarEmpresas.suspender(operadorAutenticadoId(), empresaId);
    }

    @PostMapping("/{empresaId}/reactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivar(@PathVariable UUID empresaId) {
        administrarEmpresas.reactivar(operadorAutenticadoId(), empresaId);
    }

    @PutMapping("/{empresaId}/datos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizarDatos(@PathVariable UUID empresaId, @Valid @RequestBody ActualizarDatosRequest req) {
        administrarEmpresas.actualizarDatos(operadorAutenticadoId(), empresaId, new DatosEmpresaConsola(
                req.nombreLegal(), req.representanteLegal(), req.correo(), req.telefono(), req.sitioWeb()));
    }

    @PutMapping("/{empresaId}/marca")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizarMarca(@PathVariable UUID empresaId, @Valid @RequestBody ActualizarMarcaRequest req) {
        administrarEmpresas.actualizarMarca(operadorAutenticadoId(), empresaId, new MarcaConsola(
                req.colorPrimario(), req.colorSecundario(), req.urlLogo(),
                req.tipoLogin(), req.tipoPantallaPrincipal()));
    }

    @PostMapping("/{empresaId}/modulos/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activarModulo(@PathVariable UUID empresaId, @PathVariable String codigo) {
        administrarEmpresas.activarModulo(operadorAutenticadoId(), empresaId, codigo);
    }

    @DeleteMapping("/{empresaId}/modulos/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivarModulo(@PathVariable UUID empresaId, @PathVariable String codigo) {
        administrarEmpresas.desactivarModulo(operadorAutenticadoId(), empresaId, codigo);
    }

    private static EmpresaConsolaResponse aRespuesta(EmpresaParaConsola e) {
        return new EmpresaConsolaResponse(
                e.id(), e.identificador(), e.nombreLegal(), e.dominio(), e.correo(),
                e.estado(), e.pasoAprovisionamiento(), e.estadoTarea(), e.creadaEn());
    }

    private UUID operadorAutenticadoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UUID id) {
            return id;
        }
        throw new CredencialesDeOperadorInvalidasException();
    }
}
