package com.marcablanca.platform.usuarios.infrastructure.persistencia;

import com.marcablanca.platform.usuarios.domain.UsuarioPerfil;
import com.marcablanca.platform.usuarios.domain.port.out.RepositorioUsuarioPerfiles;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RepositorioUsuarioPerfilesJpaAdapter implements RepositorioUsuarioPerfiles {

    private final SpringDataUsuarioPerfilRepository jpaRepository;

    public RepositorioUsuarioPerfilesJpaAdapter(SpringDataUsuarioPerfilRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<UsuarioPerfil> buscarPorUsuarioId(Long usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).map(this::aDominio);
    }

    @Override
    public UsuarioPerfil guardar(UsuarioPerfil perfil) {
        UsuarioPerfilJpaEntity entidad = new UsuarioPerfilJpaEntity(
                perfil.getId(), perfil.getUuid(), perfil.getUsuarioId(), perfil.getIdEmpleado(),
                perfil.getUrlFoto(), perfil.getCedula(), perfil.getTipoDocumento(), perfil.getFechaNacimiento(),
                perfil.getTelefono(), perfil.getDireccion(), perfil.getContactoEmergencia(),
                perfil.getTelefonoEmergencia(), perfil.getZona(), perfil.getCuadrillaId(), perfil.getEstadoLaboral()
        );
        return aDominio(jpaRepository.save(entidad));
    }

    private UsuarioPerfil aDominio(UsuarioPerfilJpaEntity e) {
        return new UsuarioPerfil(e.getId(), e.getUuid(), e.getUsuarioId(), e.getIdEmpleado(), e.getUrlFoto(),
                e.getCedula(), e.getTipoDocumento(), e.getFechaNacimiento(), e.getTelefono(), e.getDireccion(),
                e.getContactoEmergencia(), e.getTelefonoEmergencia(), e.getZona(), e.getCuadrillaId(), e.getEstadoLaboral());
    }
}