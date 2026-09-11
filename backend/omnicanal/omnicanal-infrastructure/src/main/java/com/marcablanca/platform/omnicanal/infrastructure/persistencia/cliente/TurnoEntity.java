package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.marcablanca.platform.omnicanal.domain.AutorTurno;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_turnos_conversacion_liwa", schema = "omnicanal")
class TurnoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "conversacion_id", nullable = false)
    private Long conversacionId;

    @Column(nullable = false)
    private Integer orden;

    @Convert(converter = ConversorAutorTurno.class)
    @Column(nullable = false, length = 20)
    private AutorTurno autor;

    @Column(name = "nombre_autor")
    private String nombreAutor;

    @Column(nullable = false)
    private String mensaje;

    @Column(name = "ocurrido_en")
    private OffsetDateTime ocurridoEn;

    protected TurnoEntity() {
    }

    TurnoEntity(Long conversacionId, int orden, AutorTurno autor, String nombreAutor, String mensaje,
                OffsetDateTime ocurridoEn) {
        this.uuid = UUID.randomUUID();
        this.conversacionId = conversacionId;
        this.orden = orden;
        this.autor = autor;
        this.nombreAutor = nombreAutor;
        this.mensaje = mensaje;
        this.ocurridoEn = ocurridoEn;
    }

    Long getId() {
        return id;
    }

    UUID getUuid() {
        return uuid;
    }

    Long getConversacionId() {
        return conversacionId;
    }

    Integer getOrden() {
        return orden;
    }

    AutorTurno getAutor() {
        return autor;
    }

    String getNombreAutor() {
        return nombreAutor;
    }

    String getMensaje() {
        return mensaje;
    }

    OffsetDateTime getOcurridoEn() {
        return ocurridoEn;
    }
}
