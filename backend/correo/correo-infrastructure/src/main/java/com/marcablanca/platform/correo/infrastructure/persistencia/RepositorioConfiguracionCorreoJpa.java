package com.marcablanca.platform.correo.infrastructure.persistencia;

import com.marcablanca.platform.correo.application.port.out.RepositorioConfiguracionCorreo;
import com.marcablanca.platform.correo.domain.ConfiguracionSmtp;
import com.marcablanca.platform.correo.infrastructure.CifradorDeCorreo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class RepositorioConfiguracionCorreoJpa implements RepositorioConfiguracionCorreo {

    private final ConfiguracionCorreoJpaRepository jpa;
    private final CifradorDeCorreo cifrador;

    RepositorioConfiguracionCorreoJpa(ConfiguracionCorreoJpaRepository jpa, CifradorDeCorreo cifrador) {
        this.jpa = jpa;
        this.cifrador = cifrador;
    }

    @Override
    public ConfiguracionSmtp crear(String remitenteNombre, String remitenteCorreo, String responderA, String host,
                                    int puerto, String usuario, String secretoRef, String seguridad, String clave) {
        var e = new ConfiguracionCorreoEntity(remitenteNombre, remitenteCorreo, responderA, host, puerto, usuario,
                secretoRef, seguridad, cifrador.cifrar(clave));
        return mapear(jpa.save(e));
    }

    @Override
    public ConfiguracionSmtp actualizar(UUID id, String remitenteNombre, String remitenteCorreo, String responderA,
                                         String host, int puerto, String usuario, String secretoRef,
                                         String seguridad, String clave) {
        var e = jpa.findByUuid(id).orElseThrow();
        e.actualizar(remitenteNombre, remitenteCorreo, responderA, host, puerto, usuario, secretoRef, seguridad);
        e.actualizarClave(cifrador.cifrar(clave));
        return mapear(jpa.save(e));
    }

    @Override
    public Optional<String> obtenerClaveDescifrada(UUID id) {
        return jpa.findByUuid(id).map(ConfiguracionCorreoEntity::getClaveCifrada).map(cifrador::descifrar);
    }

    @Override
    public Optional<ConfiguracionSmtp> buscarActiva() {
        return jpa.findByEsActivaTrue().map(this::mapear);
    }

    @Override
    @Transactional("transactionManager")
    public void desactivarTodasMenos(UUID id) {
        jpa.desactivarTodasMenos(id);
    }

    @Override
    public ConfiguracionSmtp marcarActiva(UUID id) {
        var e = jpa.findByUuid(id).orElseThrow();
        e.marcarActiva(true);
        return mapear(jpa.save(e));
    }

    @Override
    public Optional<ConfiguracionSmtp> buscarPorId(UUID id) {
        return jpa.findByUuid(id).map(this::mapear);
    }

    @Override
    public List<ConfiguracionSmtp> listarTodas() {
        return jpa.findAll().stream().map(this::mapear).toList();
    }

    @Override
    @Transactional("transactionManager")
    public void eliminar(UUID id) {
        jpa.deleteByUuid(id);
    }

    @Override
    public boolean existeConCorreo(String remitenteCorreo, UUID excluirId) {
        return excluirId == null
                ? jpa.existsByRemitenteCorreoIgnoreCase(remitenteCorreo)
                : jpa.existsByRemitenteCorreoIgnoreCaseAndUuidNot(remitenteCorreo, excluirId);
    }

    private ConfiguracionSmtp mapear(ConfiguracionCorreoEntity e) {
        return new ConfiguracionSmtp(e.getId(), e.getUuid(), e.getRemitenteNombre(), e.getRemitenteCorreo(),
                e.getResponderA(), e.getHost(), e.getPuerto(), e.getUsuario(), e.getSecretoRef(), e.getSeguridad(),
                e.isEsActiva(), e.getClaveCifrada() != null, e.getCreadoEn(), e.getActualizadoEn());
    }
}
