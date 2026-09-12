package com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente;

import com.marcablanca.platform.empresas.application.ContextoEmpresaActual;
import com.marcablanca.platform.omnicanal.domain.AbandonadoPor;
import com.marcablanca.platform.omnicanal.domain.AutorTurno;
import com.marcablanca.platform.omnicanal.domain.Resultado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Datos de demostracion para el panel de Omnicanal (Liwa): conversaciones,
 * turnos, casos y analisis IA realistas, para poder ver/probar el panel y el
 * socket en tiempo real sin depender de trafico real de WhatsApp.
 *
 * Apagado por defecto -- solo corre si se define la property
 * app.seed.omnicanal.empresa (el identificador/slug de la empresa a
 * sembrar), por ejemplo:
 *
 *   mvn -pl bootstrap spring-boot:run \
 *     -Dspring-boot.run.arguments=--app.seed.omnicanal.empresa=demo_liwa
 *
 * Es idempotente: si la empresa ya tiene conversaciones, no hace nada (para
 * poder dejar la property puesta sin duplicar datos en cada arranque).
 *
 * Vive en este paquete (no en un paquete "seed" aparte) porque necesita los
 * constructores/setters package-private de las entidades JPA de este modulo
 * (ConversacionEntity, TurnoEntity, CasoEntity, AnalisisEntity) -- son
 * package-private a proposito, para que nadie fuera de la infraestructura de
 * persistencia del cliente las use directo (ver comentario en cada entidad).
 */
@Component
@ConditionalOnProperty(prefix = "app.seed.omnicanal", name = "empresa")
public class SembradorDeDatosLiwaDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SembradorDeDatosLiwaDemo.class);
    private static final DateTimeFormatter FORMATO_HISTORIAL = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mma", Locale.US);

    private final ConversacionJpaRepository conversaciones;
    private final TurnoJpaRepository turnos;
    private final CasoJpaRepository casos;
    private final AnalisisJpaRepository analisis;

    @Value("${app.seed.omnicanal.empresa}")
    private String identificadorEmpresa;

    @Value("${app.seed.omnicanal.conversaciones:60}")
    private int cantidadConversaciones;

    private final Random random = new Random(42);

    public SembradorDeDatosLiwaDemo(ConversacionJpaRepository conversaciones, TurnoJpaRepository turnos,
                                     CasoJpaRepository casos, AnalisisJpaRepository analisis) {
        this.conversaciones = conversaciones;
        this.turnos = turnos;
        this.casos = casos;
        this.analisis = analisis;
    }

    @Override
    public void run(ApplicationArguments args) {
        ContextoEmpresaActual.establecer(identificadorEmpresa);
        try {
            sembrar();
        } finally {
            ContextoEmpresaActual.limpiar();
        }
    }

    // Nota: sin @Transactional a proposito -- este metodo se llama con
    // "this.sembrar()" desde run() (mismo objeto), y el proxy AOP de Spring
    // no intercepta auto-invocaciones, asi que una anotacion aca seria
    // ignorada en silencio. cada conversaciones.save()/turnos.save()/etc. ya
    // corre en su propia transaccion (JpaRepository es transaccional por
    // defecto) -- suficiente para un seeder donde la idempotencia real la da
    // el chequeo de conversaciones.count() > 0 de arriba.
    private void sembrar() {
        if (conversaciones.count() > 0) {
            log.info("Sembrador Liwa: '{}' ya tiene conversaciones, no se siembra de nuevo.", identificadorEmpresa);
            return;
        }

        log.info("Sembrador Liwa: creando {} conversaciones de demo para '{}'...", cantidadConversaciones,
                identificadorEmpresa);

        int procesadas = 0;
        int pendientes = 0;
        for (int i = 0; i < cantidadConversaciones; i++) {
            Guion guion = Guion.aleatorio(random);
            boolean seProcesa = random.nextInt(100) < 80; // ~80% ya analizadas, 20% pendientes
            crearConversacion(guion, seProcesa);
            if (seProcesa) {
                procesadas++;
            } else {
                pendientes++;
            }
        }

        log.info("Sembrador Liwa: listo -- {} conversaciones ({} analizadas, {} pendientes) para '{}'.",
                cantidadConversaciones, procesadas, pendientes, identificadorEmpresa);
    }

    private void crearConversacion(Guion guion, boolean seProcesa) {
        String idContacto = idContactoAleatorio();
        String nombreContacto = random.nextInt(100) < 85 ? NOMBRES[random.nextInt(NOMBRES.length)] : null;
        OffsetDateTime inicio = OffsetDateTime.now().minusMinutes(random.nextInt(60 * 24 * 30));

        List<MensajeGenerado> mensajes = new ArrayList<>();
        OffsetDateTime cursor = inicio;
        for (Guion.Turno t : guion.turnos()) {
            cursor = cursor.plusMinutes(1 + random.nextInt(8));
            String nombreAutor = switch (t.autor()) {
                case CLIENTE -> "Usuario";
                case BOT -> "bot";
                case ASESOR -> ASESORES[random.nextInt(ASESORES.length)];
            };
            mensajes.add(new MensajeGenerado(t.autor(), nombreAutor, t.texto(), cursor));
        }
        OffsetDateTime archivadaEn = cursor;

        String historial = mensajes.stream()
                .map(m -> m.nombreAutor() + " (" + FORMATO_HISTORIAL.format(m.fecha()).toLowerCase(Locale.US) + "): "
                        + m.texto())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        boolean esDeAds = random.nextInt(100) < 15;

        ConversacionEntity conversacion = new ConversacionEntity(idContacto, nombreContacto, historial, "{}", esDeAds);
        conversacion.marcarSoloArchivado(esDeAds, archivadaEn);
        conversacion = conversaciones.save(conversacion);

        for (int orden = 0; orden < mensajes.size(); orden++) {
            MensajeGenerado m = mensajes.get(orden);
            turnos.save(new TurnoEntity(conversacion.getId(), orden, m.autor(), m.nombreAutor(), m.texto(), m.fecha()));
        }

        CasoEntity caso = new CasoEntity(conversacion.getId(), 0, mensajes.size() - 1, esDeAds);
        caso.marcarProcesada(seProcesa);
        caso = casos.save(caso);

        if (seProcesa) {
            analisis.save(analisisAleatorio(caso.getId(), idContacto, guion, mensajes, esDeAds));
        }
    }

    private AnalisisEntity analisisAleatorio(Long casoId, String idContacto, Guion guion,
                                              List<MensajeGenerado> mensajes, boolean esDeAds) {
        Resultado resultado = pesoAleatorio(RESULTADOS, PESOS_RESULTADO);
        boolean fcr = resultado == Resultado.RESUELTO && random.nextInt(100) < 70;
        String sentimientoInicial = pesoAleatorio(SENTIMIENTOS, PESOS_SENTIMIENTO_INICIAL);
        String sentimientoFinal = resultado == Resultado.RESUELTO
                ? pesoAleatorio(SENTIMIENTOS, PESOS_SENTIMIENTO_FINAL_BUENO)
                : pesoAleatorio(SENTIMIENTOS, PESOS_SENTIMIENTO_FINAL_MALO);

        boolean abandono = resultado == Resultado.NO_RESUELTO && random.nextInt(100) < 40;
        AbandonadoPor abandonadoPor = abandono ? (random.nextBoolean() ? AbandonadoPor.ASESOR : AbandonadoPor.CLIENTE) : null;

        List<String> banderas = new ArrayList<>();
        if (resultado != Resultado.RESUELTO && random.nextInt(100) < 25) banderas.add("sin_respuesta");
        if (random.nextInt(100) < 15) banderas.add("espero_demasiado");
        if (random.nextInt(100) < 8) banderas.add("trato_inadecuado");
        if (resultado == Resultado.ESCALADO && random.nextInt(100) < 30) banderas.add("gestion_pendiente");

        boolean oportunidadVenta = "ventas".equals(guion.motivo()) && random.nextInt(100) < 60;
        boolean ventaConfirmada = oportunidadVenta && random.nextInt(100) < 50;

        String municipio = municipioAleatorio();
        MensajeGenerado primero = mensajes.get(0);
        MensajeGenerado primeraRespuestaEmpresa = mensajes.stream()
                .filter(m -> m.autor() != AutorTurno.CLIENTE)
                .findFirst().orElse(primero);
        MensajeGenerado ultimo = mensajes.get(mensajes.size() - 1);

        AnalisisEntity a = AnalisisEntity.nueva(casoId, idContacto);
        a.setCampos(
                "Atencion al cliente", municipio, municipio == null ? null : BARRIOS[random.nextInt(BARRIOS.length)],
                "Oficina virtual", guion.motivo(), guion.submotivo(), guion.resumenMotivo(), resumenDesenlace(resultado),
                sentimientoInicial, sentimientoFinal, resultado, fcr,
                ESFUERZOS[random.nextInt(ESFUERZOS.length)], guion.temas(), banderas,
                oportunidadVenta, ventaConfirmada, random.nextInt(100) < 5, abandono, abandonadoPor,
                esDeAds, "gpt-4.1-mini", razonamiento(guion, resultado),
                ultimo.fecha(), primero.fecha(), primeraRespuestaEmpresa.fecha(), ultimo.fecha());
        return a;
    }

    private String resumenDesenlace(Resultado resultado) {
        return switch (resultado) {
            case RESUELTO -> "El asesor resolvio la solicitud del cliente dentro de la misma conversacion.";
            case NO_RESUELTO -> "La conversacion termino sin una solucion clara para el cliente.";
            case ESCALADO -> "El caso se escalo a un area especializada para seguimiento.";
        };
    }

    private String razonamiento(Guion guion, Resultado resultado) {
        return "Se identifico un contacto por " + guion.motivo() + ". " + resumenDesenlace(resultado);
    }

    private String idContactoAleatorio() {
        StringBuilder sb = new StringBuilder("573");
        sb.append(1 + random.nextInt(5));
        for (int i = 0; i < 8; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private String municipioAleatorio() {
        int r = random.nextInt(100);
        if (r < 8) return null; // sin municipio -- caso real que el dashboard de calidad cuenta aparte
        if (r < 18) return OTROS_MUNICIPIOS[random.nextInt(OTROS_MUNICIPIOS.length)]; // fuera de la whitelist -> "Otros"
        return MUNICIPIOS_WHITELIST[random.nextInt(MUNICIPIOS_WHITELIST.length)];
    }

    private <T> T pesoAleatorio(T[] valores, int... pesos) {
        int total = 0;
        for (int p : pesos) total += p;
        int r = random.nextInt(total);
        int acumulado = 0;
        for (int i = 0; i < valores.length; i++) {
            acumulado += pesos[i];
            if (r < acumulado) return valores[i];
        }
        return valores[valores.length - 1];
    }

    private record MensajeGenerado(AutorTurno autor, String nombreAutor, String texto, OffsetDateTime fecha) {
    }

    private static final String[] NOMBRES = {
            "Carlos Perez", "Maria Gomez", "Luis Ramirez", "Ana Torres", "Jorge Ipuana", "Diana Epieyu",
            "Kevin Brito", "Yulieth Mengual", "Camilo Fernandez", "Laura Ustate",
    };
    private static final String[] ASESORES = {"Valentina Rios", "Sergio Palmar", "Natalia Uriana"};
    private static final String[] BARRIOS = {"Centro", "Los Olivos", "Villa del Rio", "El Carmen", "San Jose"};
    private static final String[] ESFUERZOS = {"bajo", "medio", "alto"};
    private static final String[] SENTIMIENTOS = {"positivo", "neutral", "negativo"};
    private static final int[] PESOS_SENTIMIENTO_INICIAL = {20, 35, 45};
    private static final int[] PESOS_SENTIMIENTO_FINAL_BUENO = {70, 25, 5};
    private static final int[] PESOS_SENTIMIENTO_FINAL_MALO = {10, 30, 60};
    private static final Resultado[] RESULTADOS = {Resultado.RESUELTO, Resultado.NO_RESUELTO, Resultado.ESCALADO};
    private static final int[] PESOS_RESULTADO = {60, 25, 15};

    // Misma whitelist que usa el frontend (DashboardCalidad) para agrupar por
    // municipio -- todo lo demas cae en "Otros".
    private static final String[] MUNICIPIOS_WHITELIST = {
            "Albania", "Hatonuevo", "Fonseca", "San Juan", "Villa Martin", "Mushaisa", "Molino",
    };
    private static final String[] OTROS_MUNICIPIOS = {"Riohacha", "Maicao", "Uribia", "Barrancas"};

    /** Guion de una conversacion tipo: motivo + turnos alternados cliente/empresa. */
    private record Guion(String motivo, String submotivo, String resumenMotivo, List<String> temas,
                          List<Turno> turnos) {

        record Turno(AutorTurno autor, String texto) {
        }

        static Guion aleatorio(Random random) {
            Plantilla p = PLANTILLAS[random.nextInt(PLANTILLAS.length)];
            List<Turno> turnos = new ArrayList<>();
            for (int i = 0; i < p.mensajes.length; i++) {
                AutorTurno autor = (i % 2 == 0) ? AutorTurno.CLIENTE : (random.nextInt(100) < 30 ? AutorTurno.BOT : AutorTurno.ASESOR);
                turnos.add(new Turno(autor, p.mensajes[i]));
            }
            return new Guion(p.motivo, p.submotivo, p.resumenMotivo, List.of(p.temas), turnos);
        }

        private record Plantilla(String motivo, String submotivo, String resumenMotivo, String[] temas, String[] mensajes) {
        }

        private static final Plantilla[] PLANTILLAS = {
                new Plantilla("soporte", "sin_internet", "El cliente reporta que no tiene servicio de internet.",
                        new String[]{"soporte", "internet"}, new String[]{
                        "Buenas, no tengo internet desde esta manana, pueden revisar?",
                        "Claro, con gusto. Me confirma su numero de contrato para revisar el estado del servicio?",
                        "Si, es el 10234.",
                        "Gracias, veo que el equipo esta reportando sin senal. Vamos a reiniciarlo de forma remota, deme unos minutos.",
                        "Listo, ya tengo internet de nuevo, muchas gracias!",
                        "Perfecto, quedamos atentos por si se vuelve a presentar. Que tenga buen dia.",
                }),
                new Plantilla("facturacion", "cobro_duplicado", "El cliente indica que le llego un cobro duplicado.",
                        new String[]{"facturacion"}, new String[]{
                        "Hola, me llego la factura duplicada este mes, pueden revisar?",
                        "Claro, permitame verificar su cuenta con el numero de contrato.",
                        "Es el 88452.",
                        "Ya veo el registro, efectivamente hay un cobro duplicado. Vamos a generar la nota credito.",
                        "Muchas gracias, quedo atento a la confirmacion.",
                }),
                new Plantilla("reconexion", "pago_no_reflejado", "El cliente ya pago pero el servicio sigue suspendido.",
                        new String[]{"reconexion", "pagos"}, new String[]{
                        "Buenas tardes, ya pague la factura pero el servicio sigue cortado.",
                        "Entiendo, me puede compartir el comprobante de pago por favor?",
                        "Si, aqui esta el comprobante.",
                        "Gracias, ya veo el pago reflejado. Voy a solicitar la reconexion inmediata.",
                        "Listo, ya me llego el internet de nuevo, gracias por la ayuda.",
                }),
                new Plantilla("ventas", "plan_nuevo", "El cliente pregunta por planes nuevos de internet.",
                        new String[]{"ventas", "planes"}, new String[]{
                        "Hola, quiero saber que planes de internet tienen disponibles.",
                        "Con gusto! Tenemos planes desde 30 hasta 100 megas, le interesa alguno en especial?",
                        "Me interesa el de 50 megas, cuanto cuesta?",
                        "El plan de 50 megas tiene un costo mensual de 65.000 pesos, incluye instalacion gratis.",
                        "Perfecto, como hago para contratarlo?",
                        "Le puedo programar una visita tecnica esta semana, me confirma su direccion?",
                }),
                new Plantilla("pqr", "atencion_deficiente", "El cliente presenta una queja por la atencion recibida.",
                        new String[]{"pqr"}, new String[]{
                        "Quiero poner una queja, el tecnico que me visito no soluciono nada.",
                        "Lamento mucho la situacion, me puede contar que paso exactamente?",
                        "Vino, reviso el router y se fue diciendo que estaba bien, pero sigo sin servicio.",
                        "Entiendo su molestia, vamos a escalar su caso a supervision tecnica para una revision prioritaria.",
                }),
                new Plantilla("cobertura", "consulta_zona", "El cliente pregunta si hay cobertura en su zona.",
                        new String[]{"cobertura"}, new String[]{
                        "Buenas, tienen cobertura en mi barrio?",
                        "Claro, me puede indicar el municipio y barrio para verificar?",
                        "Vivo en el barrio Centro.",
                        "Perfecto, si tenemos cobertura en esa zona. Le puedo agendar una visita para instalacion.",
                }),
                new Plantilla("informacion", "horarios", "El cliente pregunta por horarios de atencion.",
                        new String[]{"informacion"}, new String[]{
                        "Hola, hasta que hora atienden en oficina?",
                        "Atendemos de lunes a sabado de 8am a 5pm.",
                        "Perfecto, muchas gracias.",
                }),
        };
    }
}
