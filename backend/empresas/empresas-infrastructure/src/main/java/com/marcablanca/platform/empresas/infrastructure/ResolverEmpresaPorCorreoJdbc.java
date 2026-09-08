package com.marcablanca.platform.empresas.infrastructure;

import com.marcablanca.platform.empresas.application.port.in.ResolverEmpresaPorCorreo;
import com.marcablanca.platform.empresas.application.port.out.RepositorioEmpresaConexiones;
import com.marcablanca.platform.empresas.domain.EmpresaConexion;
import com.marcablanca.platform.empresas.domain.EmpresaConexionDeEmpresa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * No existe (todavia) un directorio central correo->empresa: cada empresa
 * vive en su propia base, y el correo solo es unico DENTRO de esa base. Para
 * que el login no obligue a escribir el identificador de la empresa, esta
 * implementacion recorre las conexiones activas y pregunta en cada una si el
 * correo existe.
 *
 * Limitaciones conocidas, aceptables para el tamano actual de la plataforma
 * pero a revisar si el numero de empresas activas crece mucho:
 *  - Una consulta por empresa activa en cada intento de resolucion (no
 *    escala linealmente bien con cientos/miles de empresas).
 *  - Si el mismo correo existe en mas de una empresa, no se puede saber a
 *    cual quiere entrar el usuario -- se trata como "no encontrado" en vez
 *    de listarlas, para no revelar en que otras empresas tiene cuenta.
 *
 * La solucion de fondo (un directorio correo->empresa mantenido al crear/
 * editar usuarios en cualquier tenant) queda pendiente; ver ADR si se
 * decide construirlo.
 */
@Component
class ResolverEmpresaPorCorreoJdbc implements ResolverEmpresaPorCorreo {

    private static final Logger log = LoggerFactory.getLogger(ResolverEmpresaPorCorreoJdbc.class);

    private final RepositorioEmpresaConexiones repositorioEmpresaConexiones;
    private final String usuario;
    private final String clave;

    ResolverEmpresaPorCorreoJdbc(
            RepositorioEmpresaConexiones repositorioEmpresaConexiones,
            @Value("${app.empresas.conexion-cliente.usuario:guajiranet_owner}") String usuario,
            @Value("${app.empresas.conexion-cliente.clave:guajiranet_owner}") String clave) {
        this.repositorioEmpresaConexiones = repositorioEmpresaConexiones;
        this.usuario = usuario;
        this.clave = clave;
    }

    @Override
    public Optional<String> ejecutar(String correo) {
        List<EmpresaConexionDeEmpresa> activas = repositorioEmpresaConexiones.listarConexionesActivas();
        String encontrada = null;
        for (EmpresaConexionDeEmpresa empresa : activas) {
            if (!existeCorreo(empresa.conexion(), correo)) {
                continue;
            }
            if (encontrada != null) {
                log.warn("El correo {} existe en mas de una empresa activa; se trata como no encontrado.", correo);
                return Optional.empty();
            }
            encontrada = empresa.identificadorEmpresa();
        }
        return Optional.ofNullable(encontrada);
    }

    private boolean existeCorreo(EmpresaConexion conexion, String correo) {
        String url = "jdbc:postgresql://" + conexion.host() + ":" + conexion.puerto() + "/" + conexion.nombreBd();
        DriverManagerDataSource ds = new DriverManagerDataSource(url, usuario, clave);
        ds.setDriverClassName("org.postgresql.Driver");
        try {
            Integer conteo = new JdbcTemplate(ds).queryForObject(
                    "select count(*) from seguridad.tbl_usuarios where correo = ? and es_activo = true",
                    Integer.class, correo);
            return conteo != null && conteo > 0;
        } catch (RuntimeException e) {
            log.warn("No se pudo consultar la base {} al resolver empresa por correo: {}",
                    conexion.nombreBd(), e.getMessage());
            return false;
        }
    }
}
