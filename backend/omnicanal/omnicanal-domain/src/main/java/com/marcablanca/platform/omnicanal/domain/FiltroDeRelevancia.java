package com.marcablanca.platform.omnicanal.domain;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Decide que turno es contenido real de atencion y cual es ruido (menu,
 * encuesta, acuse automatico, despedida). Es la logica mas fina de todo el
 * modulo -- portada tal cual del pipeline anterior, cada regla tiene un motivo
 * de negocio concreto detras.
 *
 * El vocabulario que cambia por empresa (opciones de menu, marcadores de
 * encuesta, frases-marca de cierre, nombre comercial como "saludo") llega en
 * el PerfilDeAnalisisOmnicanal. Las reglas genericas (acuses de recibo,
 * patrones de menu numerado, "quiero informacion", despedidas de cortesia) son
 * las mismas para cualquier empresa y siguen aca como patrones fijos.
 */
public final class FiltroDeRelevancia {

    private static final Pattern PATRON_MENU_1 =
            Pattern.compile("\\b(marca|marque|selecciona|seleccione|digita|digite|escribe|escriba)\\b[^.]{0,80}\\b(opcion|numero)\\b");
    private static final Pattern PATRON_MENU_2 = Pattern.compile("^\\s*(?:\\d\\s*[).\\-]\\s*\\w[^\\n]{0,60}){2,}$");
    private static final Pattern PATRON_QUIERO_INFO =
            Pattern.compile("^[¡!.,\\s]*(hola[¡!.,\\s]*)?(quiero\\s*)?(mas|más)?\\s*informaci(o|ó)n\\s*\\.?\\s*$");
    private static final Pattern PATRON_NO_SI_CORTO = Pattern.compile("^(no|si|sí)\\s+\\S");
    private static final Pattern PATRON_ACUSE_1 = Pattern.compile(
            "\\b(en breve|en un momento|pronto|ya mismo)\\b[^.]*\\b(asesor|agente|te atende|lo atende|le atende|atendemos|comunicamos)");
    private static final Pattern PATRON_ACUSE_2 = Pattern.compile(
            "un asesor (se comunicara|te contactara|lo contactara|le contactara|te atendera|lo atendera|le atendera)");
    private static final String SALUDO_BASE =
            "hola+|buenas?|buenos|dias|tardes|noches|hey|saludos|cordial saludo|bienvenido|bienvenida|"
                    + "un gusto saludarte|estimado|estimada|senor|senora|amigo|amiga";
    private static final Pattern PATRON_ATENTOS = Pattern.compile(
            "\\b(estaremos atentos|quedamos atentos|cualquier (inquietud|cosa|duda) (estamos|quedamos) (atentos|pendientes))\\b");
    private static final Pattern PATRON_DESPEDIDA = Pattern.compile(
            "\\b(feliz (dia|tarde|noche)|que tengas (un )?(buen|bonito|excelente) dia)\\b");
    private static final Pattern PATRON_FORM =
            Pattern.compile("docs\\.google\\.com/forms|forms\\.gle/", Pattern.CASE_INSENSITIVE);

    private final Set<String> opcionesMenu;
    private final List<String> marcadoresEncuesta;
    private final List<String> frasesMarcaRuido;
    private final Pattern patronSaludo;

    public FiltroDeRelevancia(PerfilDeAnalisisOmnicanal perfil) {
        // Las tres colecciones se comparan contra texto YA normalizado (sin
        // acentos, minusculas), asi que sus entradas se normalizan aca: da igual
        // si el perfil las trae con tildes/mayusculas.
        this.opcionesMenu = perfil.opcionesMenu().stream()
                .map(FiltroDeRelevancia::normalizar).collect(java.util.stream.Collectors.toUnmodifiableSet());
        this.marcadoresEncuesta = perfil.marcadoresEncuesta().stream()
                .map(FiltroDeRelevancia::normalizar).toList();
        this.frasesMarcaRuido = perfil.frasesMarcaRuido().stream()
                .map(FiltroDeRelevancia::normalizar).toList();
        String nombre = normalizar(perfil.nombreEmpresa() == null ? "" : perfil.nombreEmpresa());
        String alternativas = nombre.isBlank() ? SALUDO_BASE : SALUDO_BASE + "|" + Pattern.quote(nombre);
        this.patronSaludo = Pattern.compile("\\b(" + alternativas + ")\\b");
    }

    public static String normalizar(String texto) {
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase().strip().replaceAll("\\s+", " ");
    }

    public boolean esTurnoDeEncuesta(String mensaje) {
        String t = normalizar(mensaje);
        if (PATRON_FORM.matcher(t).find()) {
            return true;
        }
        for (String marcador : marcadoresEncuesta) {
            if (t.contains(marcador)) {
                return true;
            }
        }
        return false;
    }

    public boolean esTurnoNoRelevante(String mensaje) {
        String t = normalizar(mensaje);
        if (t.isEmpty()) {
            return true;
        }

        if (PATRON_NO_SI_CORTO.matcher(t).find()) {
            return false;
        }

        if (esTurnoDeEncuesta(mensaje)) {
            return true;
        }

        boolean esSoloAcuseDeRecibo = (PATRON_ACUSE_1.matcher(t).find()
                || PATRON_ACUSE_2.matcher(t).find()
                || t.contains("hemos recibido tu mensaje")
                || t.contains("gracias por escribirnos")
                || t.contains("gracias por contactarnos")
                || t.contains("en breve te atenderemos")) && t.length() <= 150;
        if (esSoloAcuseDeRecibo) {
            return true;
        }

        if (PATRON_MENU_1.matcher(t).find() || PATRON_MENU_2.matcher(t).matches()) {
            return true;
        }

        if (PATRON_QUIERO_INFO.matcher(t).matches()) {
            return true;
        }

        String opcion = t.replaceAll("[^a-z0-9 ]", "").strip();
        if (opcionesMenu.contains(opcion)) {
            return true;
        }

        String restante = patronSaludo.matcher(t).replaceAll("").replaceAll("[^a-z0-9]", "");
        if (restante.length() <= 2 && !t.contains("?")) {
            return true;
        }

        boolean esSoloColetillaDeAtentos = PATRON_ATENTOS.matcher(t).find() && t.length() <= 100;
        boolean esSoloDespedidaCortes = PATRON_DESPEDIDA.matcher(t).find() && t.length() <= 100;
        if (esSoloColetillaDeAtentos || esSoloDespedidaCortes) {
            return true;
        }

        for (String frase : frasesMarcaRuido) {
            if (t.contains(frase)) {
                return true;
            }
        }
        return false;
    }

    public boolean segmentoTieneContenidoReal(List<TurnoParseado> turnos) {
        return turnos.stream().anyMatch(t -> !esTurnoNoRelevante(t.mensaje()));
    }
}
