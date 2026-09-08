package com.marcablanca.platform.omnicanal.application.port.out;

import com.marcablanca.platform.omnicanal.domain.Conversacion;
import com.marcablanca.platform.omnicanal.domain.Turno;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RepositorioConversaciones {

    Optional<Conversacion> buscarPorIdContacto(String idContacto);

    Conversacion crear(String idContacto, String nombreContacto, String historialChatCompleto,
                        Map<String, Object> datosCrudos, boolean esDeAds);

    Conversacion actualizar(Long id, String historialChatCompleto, Map<String, Object> datosCrudos,
                             boolean esDeAds, OffsetDateTime archivadaEn);

    void marcarSoloArchivado(Long id, boolean esDeAds);

    List<Turno> listarTurnos(Long conversacionId);

    int siguienteOrden(Long conversacionId);

    void guardarTurnos(Long conversacionId, int ordenInicial, List<TurnoParseadoConOrden> turnos);

    record TurnoParseadoConOrden(int orden, com.marcablanca.platform.omnicanal.domain.AutorTurno autor,
                                  String nombreAutor, String mensaje, OffsetDateTime ocurridoEn) {
    }

    record Pagina<T>(long total, int pagina, int porPagina, int totalPaginas, List<T> items) {
    }

    Pagina<Conversacion> listarRecientes(String idContacto, int pagina, int porPagina,
                                          OffsetDateTime desde, OffsetDateTime hasta);
}
