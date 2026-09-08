package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.marcablanca.platform.omnicanal.domain.AbandonadoPor;
import com.marcablanca.platform.omnicanal.domain.Resultado;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tbl_conversaciones_analizadas", schema = "omnicanal")
class AnalisisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "caso_id", nullable = false, unique = true)
    private Long casoId;

    @Column(name = "id_contacto", nullable = false)
    private String idContacto;

    @Column(name = "area_destino")
    private String areaDestino;
    private String municipio;
    private String barrio;
    @Column(name = "categoria_oficina")
    private String categoriaOficina;
    @Column(name = "motivo_contacto")
    private String motivoContacto;
    private String submotivo;
    @Column(name = "resumen_motivo")
    private String resumenMotivo;
    @Column(name = "resumen_desenlace")
    private String resumenDesenlace;
    @Column(name = "sentimiento_inicial")
    private String sentimientoInicial;
    @Column(name = "sentimiento_final")
    private String sentimientoFinal;

    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    private Boolean fcr;
    @Column(name = "esfuerzo_cliente")
    private String esfuerzoCliente;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> temas;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "banderas_calidad")
    private List<String> banderasCalidad;

    @Column(name = "oportunidad_venta")
    private Boolean oportunidadVenta;
    @Column(name = "venta_confirmada_en_texto")
    private Boolean ventaConfirmadaEnTexto;
    @Column(name = "revisar_limite")
    private Boolean revisarLimite;
    private Boolean abandono;

    @Enumerated(EnumType.STRING)
    @Column(name = "abandonado_por")
    private AbandonadoPor abandonadoPor;

    @Column(name = "es_de_ads", nullable = false)
    private boolean esDeAds;

    @Column(name = "modelo_ia_usado")
    private String modeloIaUsado;

    private String razonamiento;

    @Column(name = "cerrado_en")
    private OffsetDateTime cerradoEn;
    @Column(name = "primer_mensaje_en")
    private OffsetDateTime primerMensajeEn;
    @Column(name = "primera_respuesta_en")
    private OffsetDateTime primeraRespuestaEn;
    @Column(name = "procesado_en", nullable = false)
    private OffsetDateTime procesadoEn;

    protected AnalisisEntity() {
    }

    Long getId() { return id; }
    UUID getUuid() { return uuid; }
    Long getCasoId() { return casoId; }
    String getIdContacto() { return idContacto; }
    String getAreaDestino() { return areaDestino; }
    String getMunicipio() { return municipio; }
    String getBarrio() { return barrio; }
    String getCategoriaOficina() { return categoriaOficina; }
    String getMotivoContacto() { return motivoContacto; }
    String getSubmotivo() { return submotivo; }
    String getResumenMotivo() { return resumenMotivo; }
    String getResumenDesenlace() { return resumenDesenlace; }
    String getSentimientoInicial() { return sentimientoInicial; }
    String getSentimientoFinal() { return sentimientoFinal; }
    Resultado getResultado() { return resultado; }
    Boolean getFcr() { return fcr; }
    String getEsfuerzoCliente() { return esfuerzoCliente; }
    List<String> getTemas() { return temas; }
    List<String> getBanderasCalidad() { return banderasCalidad; }
    Boolean getOportunidadVenta() { return oportunidadVenta; }
    Boolean getVentaConfirmadaEnTexto() { return ventaConfirmadaEnTexto; }
    Boolean getRevisarLimite() { return revisarLimite; }
    Boolean getAbandono() { return abandono; }
    AbandonadoPor getAbandonadoPor() { return abandonadoPor; }
    boolean isEsDeAds() { return esDeAds; }
    String getModeloIaUsado() { return modeloIaUsado; }
    String getRazonamiento() { return razonamiento; }
    OffsetDateTime getCerradoEn() { return cerradoEn; }
    OffsetDateTime getPrimerMensajeEn() { return primerMensajeEn; }
    OffsetDateTime getPrimeraRespuestaEn() { return primeraRespuestaEn; }
    OffsetDateTime getProcesadoEn() { return procesadoEn; }

    static AnalisisEntity nueva(Long casoId, String idContacto) {
        AnalisisEntity e = new AnalisisEntity();
        e.uuid = UUID.randomUUID();
        e.casoId = casoId;
        e.idContacto = idContacto;
        return e;
    }

    void setCampos(String areaDestino, String municipio, String barrio, String categoriaOficina,
                    String motivoContacto, String submotivo, String resumenMotivo, String resumenDesenlace,
                    String sentimientoInicial, String sentimientoFinal, Resultado resultado, Boolean fcr,
                    String esfuerzoCliente, List<String> temas, List<String> banderasCalidad,
                    Boolean oportunidadVenta, Boolean ventaConfirmadaEnTexto, Boolean revisarLimite,
                    Boolean abandono, AbandonadoPor abandonadoPor, boolean esDeAds, String modeloIaUsado,
                    String razonamiento, OffsetDateTime cerradoEn, OffsetDateTime primerMensajeEn,
                    OffsetDateTime primeraRespuestaEn, OffsetDateTime procesadoEn) {
        this.areaDestino = areaDestino;
        this.municipio = municipio;
        this.barrio = barrio;
        this.categoriaOficina = categoriaOficina;
        this.motivoContacto = motivoContacto;
        this.submotivo = submotivo;
        this.resumenMotivo = resumenMotivo;
        this.resumenDesenlace = resumenDesenlace;
        this.sentimientoInicial = sentimientoInicial;
        this.sentimientoFinal = sentimientoFinal;
        this.resultado = resultado;
        this.fcr = fcr;
        this.esfuerzoCliente = esfuerzoCliente;
        this.temas = temas;
        this.banderasCalidad = banderasCalidad;
        this.oportunidadVenta = oportunidadVenta;
        this.ventaConfirmadaEnTexto = ventaConfirmadaEnTexto;
        this.revisarLimite = revisarLimite;
        this.abandono = abandono;
        this.abandonadoPor = abandonadoPor;
        this.esDeAds = esDeAds;
        this.modeloIaUsado = modeloIaUsado;
        this.razonamiento = razonamiento;
        this.cerradoEn = cerradoEn;
        this.primerMensajeEn = primerMensajeEn;
        this.primeraRespuestaEn = primeraRespuestaEn;
        this.procesadoEn = procesadoEn;
    }
}
