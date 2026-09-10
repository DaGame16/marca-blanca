package com.marcablanca.platform.omnicanal.domain;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convierte el texto crudo que manda LIWA ("NOMBRE (fecha): mensaje",
 * bloques separados por doble salto de linea) en turnos estructurados.
 * Logica portada tal cual del pipeline anterior -- son reglas de negocio
 * afinadas con datos reales, no un parser generico.
 */
public final class ParseadorDeTurnos {

    private static final Pattern PATRON_TURNO = Pattern.compile("^(.+?)\\s\\(([^)]+)\\):\\s*([\\s\\S]*)$");
    private static final Set<String> NOMBRES_BOT = Set.of("yo", "bot");
    private static final Pattern PATRON_FECHA =
            Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})\\s+(\\d{1,2}):(\\d{2})\\s*([ap])\\.?\\s*\\.?\\s*m\\.?",
                    Pattern.CASE_INSENSITIVE);

    private ParseadorDeTurnos() {
    }

    public static List<TurnoParseado> parsear(String texto) {
        List<TurnoParseado> turnos = new ArrayList<>();
        if (texto == null || texto.isBlank()) {
            return turnos;
        }

        String[] bloques = texto.split("\\n\\n+");
        for (String bloqueCrudo : bloques) {
            String bloque = bloqueCrudo.strip();
            if (bloque.isEmpty()) {
                continue;
            }

            Matcher m = PATRON_TURNO.matcher(bloque);
            if (!m.matches()) {
                if (!turnos.isEmpty()) {
                    TurnoParseado anterior = turnos.get(turnos.size() - 1);
                    turnos.set(turnos.size() - 1, new TurnoParseado(
                            anterior.nombre(), anterior.fechaTexto(), anterior.fecha(),
                            anterior.mensaje() + "\n\n" + bloque, anterior.autor()));
                }
                continue;
            }

            String nombre = m.group(1).strip();
            String fechaTexto = m.group(2).strip();
            String mensaje = m.group(3).strip();
            turnos.add(new TurnoParseado(nombre, fechaTexto, parsearFecha(fechaTexto), mensaje,
                    resolverAutor(nombre)));
        }
        return turnos;
    }

    private static AutorTurno resolverAutor(String nombre) {
        String normalizado = nombre.strip().toLowerCase();
        if (normalizado.equals("usuario")) {
            return AutorTurno.CLIENTE;
        }
        if (NOMBRES_BOT.contains(normalizado)) {
            return AutorTurno.BOT;
        }
        return AutorTurno.ASESOR;
    }

    public static OffsetDateTime parsearFecha(String fechaStr) {
        Matcher m = PATRON_FECHA.matcher(fechaStr);
        if (!m.find()) {
            return null;
        }
        int anio = Integer.parseInt(m.group(1));
        int mes = Integer.parseInt(m.group(2));
        int dia = Integer.parseInt(m.group(3));
        int hora = Integer.parseInt(m.group(4));
        int minuto = Integer.parseInt(m.group(5));
        String ampm = m.group(6).toLowerCase();

        if (ampm.equals("p") && hora != 12) {
            hora += 12;
        }
        if (ampm.equals("a") && hora == 12) {
            hora = 0;
        }

        try {
            return java.time.LocalDateTime.of(anio, mes, dia, hora, minuto)
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime();
        } catch (RuntimeException _) {
            return null;
        }
    }
}
