package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.marcablanca.platform.omnicanal.domain.AutorTurno;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * omnicanal.tbl_turnos_conversacion_liwa.autor tiene un CHECK que exige
 * minusculas ('cliente', 'bot', 'asesor') -- @Enumerated(EnumType.STRING)
 * guarda el nombre del enum tal cual (CLIENTE, BOT, ASESOR), que viola ese
 * CHECK. Este converter hace la traduccion en los dos sentidos.
 */
@Converter(autoApply = false)
class ConversorAutorTurno implements AttributeConverter<AutorTurno, String> {

    @Override
    public String convertToDatabaseColumn(AutorTurno autor) {
        return autor == null ? null : autor.name().toLowerCase();
    }

    @Override
    public AutorTurno convertToEntityAttribute(String valor) {
        return valor == null ? null : AutorTurno.valueOf(valor.toUpperCase());
    }
}
