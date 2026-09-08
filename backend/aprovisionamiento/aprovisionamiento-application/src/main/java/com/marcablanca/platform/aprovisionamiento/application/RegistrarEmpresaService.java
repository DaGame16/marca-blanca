package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.RegistrarEmpresa;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaYaExisteException;
import com.marcablanca.platform.aprovisionamiento.domain.Identificador;

/**
 * Paso 1 del registro: crea la empresa en estado BORRADOR. Deriva el identificador
 * y el dominio del sitio web. No hay contrasena ni modulos todavia, y el evento de
 * aprovisionamiento se levanta recien cuando el registro se finaliza.
 */
public class RegistrarEmpresaService implements RegistrarEmpresa {

    private final RepositorioEmpresas repositorioEmpresas;
    private final String sufijoDominio;

    public RegistrarEmpresaService(RepositorioEmpresas repositorioEmpresas, String sufijoDominio) {
        this.repositorioEmpresas = repositorioEmpresas;
        this.sufijoDominio = sufijoDominio;
    }

    @Override
    public ResultadoRegistroEmpresa ejecutar(ComandoRegistrarEmpresa comando) {
        Identificador identificador = Identificador.desde(comando.sitioWeb());
        String dominio = identificador.valor() + "." + sufijoDominio;

        if (repositorioEmpresas.existePorIdentificador(identificador)) {
            throw new EmpresaYaExisteException("el identificador '" + identificador.valor() + "'");
        }
        if (repositorioEmpresas.existePorDominio(dominio)) {
            throw new EmpresaYaExisteException("el dominio '" + dominio + "'");
        }

        Empresa empresa = Empresa.registrar(
                identificador,
                dominio,
                comando.nombreEmpresa(),
                comando.representanteLegal(),
                comando.correo(),
                comando.telefono(),
                comando.sitioWeb());

        repositorioEmpresas.guardar(empresa);

        return new ResultadoRegistroEmpresa(
                empresa.getId(),
                identificador.valor(),
                dominio,
                empresa.getEstado().name().toLowerCase());
    }
}
