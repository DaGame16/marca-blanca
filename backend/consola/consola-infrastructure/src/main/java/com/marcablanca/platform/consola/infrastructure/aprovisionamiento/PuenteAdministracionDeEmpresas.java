package com.marcablanca.platform.consola.infrastructure.aprovisionamiento;

import com.marcablanca.platform.aprovisionamiento.application.DatosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.DetalleDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.ModuloDisponible;
import com.marcablanca.platform.aprovisionamiento.application.ResumenDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.ActualizarDatosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.ActualizarPersonalizacionDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.CambiarEstadoDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.GestionarModulosDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.in.ListarEmpresas;
import com.marcablanca.platform.aprovisionamiento.application.port.in.ObtenerDetalleDeEmpresa;
import com.marcablanca.platform.aprovisionamiento.domain.ColorHex;
import com.marcablanca.platform.aprovisionamiento.domain.Personalizacion;
import com.marcablanca.platform.consola.application.DatosEmpresaConsola;
import com.marcablanca.platform.consola.application.DetalleEmpresaConsola;
import com.marcablanca.platform.consola.application.DetalleEmpresaConsola.ModuloConsola;
import com.marcablanca.platform.consola.application.EmpresaParaConsola;
import com.marcablanca.platform.consola.application.MarcaConsola;
import com.marcablanca.platform.consola.application.port.out.AdministracionDeEmpresas;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Anti-Corruption Layer entre la consola y aprovisionamiento. Unico punto donde
 * la consola conoce los puertos de aprovisionamiento; traduce los DTO de la
 * consola a/desde los del otro contexto.
 */
@Component
class PuenteAdministracionDeEmpresas implements AdministracionDeEmpresas {

    private final ListarEmpresas listarEmpresas;
    private final ObtenerDetalleDeEmpresa obtenerDetalleDeEmpresa;
    private final CambiarEstadoDeEmpresa cambiarEstadoDeEmpresa;
    private final ActualizarDatosDeEmpresa actualizarDatosDeEmpresa;
    private final ActualizarPersonalizacionDeEmpresa actualizarPersonalizacionDeEmpresa;
    private final GestionarModulosDeEmpresa gestionarModulosDeEmpresa;

    PuenteAdministracionDeEmpresas(ListarEmpresas listarEmpresas,
                                   ObtenerDetalleDeEmpresa obtenerDetalleDeEmpresa,
                                   CambiarEstadoDeEmpresa cambiarEstadoDeEmpresa,
                                   ActualizarDatosDeEmpresa actualizarDatosDeEmpresa,
                                   ActualizarPersonalizacionDeEmpresa actualizarPersonalizacionDeEmpresa,
                                   GestionarModulosDeEmpresa gestionarModulosDeEmpresa) {
        this.listarEmpresas = listarEmpresas;
        this.obtenerDetalleDeEmpresa = obtenerDetalleDeEmpresa;
        this.cambiarEstadoDeEmpresa = cambiarEstadoDeEmpresa;
        this.actualizarDatosDeEmpresa = actualizarDatosDeEmpresa;
        this.actualizarPersonalizacionDeEmpresa = actualizarPersonalizacionDeEmpresa;
        this.gestionarModulosDeEmpresa = gestionarModulosDeEmpresa;
    }

    @Override
    public List<EmpresaParaConsola> listar() {
        return listarEmpresas.ejecutar().stream().map(PuenteAdministracionDeEmpresas::traducirResumen).toList();
    }

    @Override
    public Optional<DetalleEmpresaConsola> detalle(UUID empresaId) {
        return obtenerDetalleDeEmpresa.ejecutar(empresaId).map(PuenteAdministracionDeEmpresas::traducirDetalle);
    }

    @Override
    public void suspender(UUID empresaId) {
        cambiarEstadoDeEmpresa.ejecutar(empresaId, CambiarEstadoDeEmpresa.Transicion.SUSPENDER);
    }

    @Override
    public void reactivar(UUID empresaId) {
        cambiarEstadoDeEmpresa.ejecutar(empresaId, CambiarEstadoDeEmpresa.Transicion.REACTIVAR);
    }

    @Override
    public void actualizarDatos(UUID empresaId, DatosEmpresaConsola datos) {
        actualizarDatosDeEmpresa.ejecutar(empresaId, new DatosDeEmpresa(
                datos.nombreLegal(), datos.representanteLegal(), datos.correo(),
                datos.telefono(), datos.sitioWeb()));
    }

    @Override
    public void actualizarMarca(UUID empresaId, MarcaConsola marca) {
        Personalizacion personalizacion = new Personalizacion(
                aColor(marca.colorPrimario()),
                aColor(marca.colorSecundario()),
                marca.urlLogo(),
                marca.tipoLogin(),
                marca.tipoPantallaPrincipal());
        actualizarPersonalizacionDeEmpresa.ejecutar(empresaId, personalizacion);
    }

    @Override
    public void activarModulo(UUID empresaId, String codigoModulo) {
        gestionarModulosDeEmpresa.activar(empresaId, codigoModulo);
    }

    @Override
    public void desactivarModulo(UUID empresaId, String codigoModulo) {
        gestionarModulosDeEmpresa.desactivar(empresaId, codigoModulo);
    }

    private static ColorHex aColor(String valor) {
        return (valor == null || valor.isBlank()) ? null : new ColorHex(valor);
    }

    private static EmpresaParaConsola traducirResumen(ResumenDeEmpresa r) {
        return new EmpresaParaConsola(
                r.id().toString(), r.identificador(), r.nombreLegal(), r.dominio(), r.correo(),
                r.estado(), r.pasoAprovisionamiento(), r.estadoTarea(), r.creadaEn());
    }

    private static DetalleEmpresaConsola traducirDetalle(DetalleDeEmpresa d) {
        var datos = d.datos();
        List<ModuloConsola> modulos = d.modulos().stream()
                .map(PuenteAdministracionDeEmpresas::traducirModulo)
                .toList();
        return new DetalleEmpresaConsola(
                datos.id().toString(),
                datos.identificador(),
                datos.nombreLegal(),
                datos.dominio(),
                datos.representanteLegal(),
                datos.correo(),
                datos.telefono(),
                datos.sitioWeb(),
                datos.estado(),
                datos.colorPrimario(),
                datos.colorSecundario(),
                datos.urlLogo(),
                datos.tipoLogin(),
                datos.tipoPantallaPrincipal(),
                modulos);
    }

    private static ModuloConsola traducirModulo(ModuloDisponible m) {
        return new ModuloConsola(m.codigo(), m.nombre(), m.activo());
    }
}
