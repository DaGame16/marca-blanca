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
        UsuarioPerfilJpaEntity entidad = UsuarioPerfilJpaEntity.builder()
                .id(perfil.getId())
                .uuid(perfil.getUuid())
                .usuarioId(perfil.getUsuarioId())
                .idEmpleado(perfil.getIdEmpleado())
                .urlFoto(perfil.getUrlFoto())
                .cedula(perfil.getCedula())
                .tipoDocumento(perfil.getTipoDocumento())
                .fechaNacimiento(perfil.getFechaNacimiento())
                .telefono(perfil.getTelefono())
                .direccion(perfil.getDireccion())
                .contactoEmergencia(perfil.getContactoEmergencia())
                .telefonoEmergencia(perfil.getTelefonoEmergencia())
                .zona(perfil.getZona())
                .cuadrillaId(perfil.getCuadrillaId())
                .estadoLaboral(perfil.getEstadoLaboral())
                .build();
        return aDominio(jpaRepository.save(entidad));
    }

    private UsuarioPerfil aDominio(UsuarioPerfilJpaEntity e) {
        return UsuarioPerfil.builder()
                .id(e.getId())
                .uuid(e.getUuid())
                .usuarioId(e.getUsuarioId())
                .idEmpleado(e.getIdEmpleado())
                .urlFoto(e.getUrlFoto())
                .cedula(e.getCedula())
                .tipoDocumento(e.getTipoDocumento())
                .fechaNacimiento(e.getFechaNacimiento())
                .telefono(e.getTelefono())
                .direccion(e.getDireccion())
                .contactoEmergencia(e.getContactoEmergencia())
                .telefonoEmergencia(e.getTelefonoEmergencia())
                .zona(e.getZona())
                .cuadrillaId(e.getCuadrillaId())
                .estadoLaboral(e.getEstadoLaboral())
                .build();
    }
}