package com.marcablanca.platform.correo.application;

import com.marcablanca.platform.correo.application.port.in.GestionarConfiguracionCorreo;
import com.marcablanca.platform.correo.application.port.in.ProbarConfiguracionCorreo;
import com.marcablanca.platform.correo.application.port.in.ProbarConfiguracionCorreo.DatosConexion;
import com.marcablanca.platform.correo.application.port.out.RepositorioConfiguracionCorreo;
import com.marcablanca.platform.correo.domain.ConfiguracionCorreoNoEncontradaException;
import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;

import java.util.List;
import java.util.UUID;

public class GestionarConfiguracionCorreoService implements GestionarConfiguracionCorreo {

    private final RepositorioConfiguracionCorreo repositorio;
    private final ProbarConfiguracionCorreo probarConfiguracionCorreo;

    public GestionarConfiguracionCorreoService(RepositorioConfiguracionCorreo repositorio,
                                                ProbarConfiguracionCorreo probarConfiguracionCorreo) {
        this.repositorio = repositorio;
        this.probarConfiguracionCorreo = probarConfiguracionCorreo;
    }

    @Override
    public ConfiguracionSmtp crear(ComandoConfiguracionSmtp c) {
        exigirCorreoNoDuplicado(c.remitenteCorreo(), null);
        // Se manda de verdad un correo de prueba AL PROPIO remitente antes de
        // guardar -- asi no se crean configuraciones "fantasma" con host,
        // usuario o clave que en realidad no funcionan.
        probarConfiguracionCorreo.ejecutarAdHoc(aDatosConexion(c, c.clave()), c.remitenteCorreo());
        return repositorio.crear(c.remitenteNombre(), c.remitenteCorreo(), c.responderA(), c.host(), c.puerto(),
                c.usuario(), c.secretoRef(), c.seguridad(), c.clave());
    }

    @Override
    public ConfiguracionSmtp actualizar(UUID id, ComandoConfiguracionSmtp c) {
        exigirQueExista(id);
        exigirCorreoNoDuplicado(c.remitenteCorreo(), id);
        // null en c.clave() = "no cambiarla" -- para validar la conexion hay
        // que probar con la clave que de verdad va a quedar vigente.
        String claveEfectiva = c.clave() != null ? c.clave() : repositorio.obtenerClaveDescifrada(id).orElse(null);
        probarConfiguracionCorreo.ejecutarAdHoc(aDatosConexion(c, claveEfectiva), c.remitenteCorreo());
        return repositorio.actualizar(id, c.remitenteNombre(), c.remitenteCorreo(), c.responderA(), c.host(),
                c.puerto(), c.usuario(), c.secretoRef(), c.seguridad(), c.clave());
    }

    @Override
    public ConfiguracionSmtp activar(UUID id) {
        exigirQueExista(id);
        // Orden importa: desactivar las demas ANTES de activar esta -- la tabla
        // tiene un indice unico parcial que no permite 2 activas al mismo tiempo.
        repositorio.desactivarTodasMenos(id);
        return repositorio.marcarActiva(id);
    }

    @Override
    public List<ConfiguracionSmtp> listar() {
        return repositorio.listarTodas();
    }

    @Override
    public ConfiguracionSmtp buscarPorId(UUID id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new ConfiguracionCorreoNoEncontradaException(id));
    }

    @Override
    public void eliminar(UUID id) {
        ConfiguracionSmtp existente = buscarPorId(id);
        if (existente.esActiva()) {
            throw new IllegalStateException(
                    "No se puede borrar la configuracion activa -- activa otra primero.");
        }
        repositorio.eliminar(id);
    }

    private void exigirQueExista(UUID id) {
        if (repositorio.buscarPorId(id).isEmpty()) {
            throw new ConfiguracionCorreoNoEncontradaException(id);
        }
    }

    private void exigirCorreoNoDuplicado(String remitenteCorreo, UUID idEnEdicion) {
        if (repositorio.existeConCorreo(remitenteCorreo, idEnEdicion)) {
            throw new IllegalStateException(
                    "Ya existe una configuracion de correo con el remitente " + remitenteCorreo + ".");
        }
    }

    private DatosConexion aDatosConexion(ComandoConfiguracionSmtp c, String clave) {
        return new DatosConexion(c.remitenteNombre(), c.remitenteCorreo(), c.host(), c.puerto(), c.usuario(),
                c.seguridad(), clave);
    }
}
