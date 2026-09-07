package com.marcablanca.platform.aprovisionamiento.infrastructure.persistencia;

import com.marcablanca.platform.aprovisionamiento.application.port.out.RegistroDeEventos;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaRegistrada;
import com.marcablanca.platform.aprovisionamiento.domain.EventoDeDominio;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Escribe cada evento de dominio como una fila del outbox. Corre dentro de la
 * transaccion abierta por el decorador de la Capa 1: la empresa ya esta flusheada
 * y por eso se puede resolver su id serial para agregado_id.
 */
@Component
class RegistroDeEventosJpaAdapter implements RegistroDeEventos {

    static final String TIPO_EMPRESA_REGISTRADA = "empresa.aprovisionamiento_solicitado";
    private static final String AGREGADO_EMPRESA = "empresa";

    private final SpringDataEventoSalienteRepository outbox;
    private final SpringDataEmpresaRepository empresaRepo;
    private final ObjectMapper json;

    RegistroDeEventosJpaAdapter(SpringDataEventoSalienteRepository outbox,
                                SpringDataEmpresaRepository empresaRepo,
                                ObjectMapper json) {
        this.outbox = outbox;
        this.empresaRepo = empresaRepo;
        this.json = json;
    }

    @Override
    public void publicar(List<EventoDeDominio> eventos) {
        for (EventoDeDominio evento : eventos) {
            switch (evento) {
                case EmpresaRegistrada e -> guardarEmpresaRegistrada(e);
            }
        }
    }

    private void guardarEmpresaRegistrada(EmpresaRegistrada e) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("empresaUuid", e.empresaId().toString());
        payload.put("identificador", e.identificador());
        payload.put("nombreLegal", e.nombreLegal());
        payload.put("nombreComercial", e.nombreComercial());
        payload.put("dominio", e.dominio());
        payload.put("modulosSolicitados", List.copyOf(e.modulosSolicitados()));
        payload.put("ocurridoEn", e.ocurridoEn().toString());

        outbox.save(new EventoSalienteEntity(
                TIPO_EMPRESA_REGISTRADA,
                AGREGADO_EMPRESA,
                resolverAgregadoId(e.empresaId()),
                serializar(payload)));
    }

    private Long resolverAgregadoId(UUID empresaUuid) {
        return empresaRepo.buscarIdInternoPorUuid(empresaUuid)
                .orElseThrow(() -> new IllegalStateException(
                        "La empresa " + empresaUuid + " no esta persistida al publicar su evento."));
    }

    private String serializar(Map<String, Object> payload) {
        try {
            return json.writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el payload del evento de outbox.", ex);
        }
    }
}