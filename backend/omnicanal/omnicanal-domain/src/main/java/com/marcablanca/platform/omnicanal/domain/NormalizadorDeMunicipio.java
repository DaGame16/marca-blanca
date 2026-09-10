package com.marcablanca.platform.omnicanal.domain;

import java.util.List;
import java.util.Map;

/**
 * Fuzzy-matching de lugares (municipios/zonas de cobertura) contra lo que
 * devuelve la IA en texto libre. La lista de lugares conocidos, sus
 * abreviaturas y los valores que cuentan como "sin lugar" llegan en el
 * PerfilDeAnalisisOmnicanal -- son datos de cada empresa. El algoritmo
 * (Levenshtein con tolerancia, busqueda por ventana, capitalizacion) es el
 * mismo para todas.
 */
public final class NormalizadorDeMunicipio {

    private final List<String> lugaresConocidos;
    private final Map<String, String> abreviaturas;
    private final List<String> vacios;

    public NormalizadorDeMunicipio(PerfilDeAnalisisOmnicanal perfil) {
        // lugaresConocidos se normaliza al vuelo dentro del fuzzy-match y su
        // nombre crudo (con tildes) es el valor de salida, asi que se guarda tal
        // cual. En cambio las claves de abreviaturas y los "vacios" se comparan
        // contra texto ya normalizado -> se normalizan aca.
        this.lugaresConocidos = List.copyOf(perfil.lugaresConocidos());
        this.abreviaturas = perfil.abreviaturasLugar().entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        e -> FiltroDeRelevancia.normalizar(e.getKey()), Map.Entry::getValue));
        this.vacios = perfil.lugaresVacios().stream()
                .map(FiltroDeRelevancia::normalizar).toList();
    }

    public String normalizar(String crudo) {
        if (crudo == null) {
            return null;
        }
        String limpio = crudo.strip().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            return null;
        }

        String normalizado = FiltroDeRelevancia.normalizar(limpio);
        if (vacios.contains(normalizado)) {
            return null;
        }

        String abreviatura = abreviaturas.get(normalizado);
        if (abreviatura != null) {
            return abreviatura;
        }

        String completa = buscarCoincidenciaCompleta(normalizado, 0.15);
        if (completa != null) {
            return completa;
        }

        String ventana = buscarCoincidenciaPorVentana(normalizado, 0.15);
        if (ventana != null) {
            return ventana;
        }

        return capitalizarPalabras(limpio);
    }

    private static String capitalizarPalabras(String texto) {
        StringBuilder sb = new StringBuilder();
        for (String palabra : texto.toLowerCase().split(" ")) {
            if (!palabra.isEmpty()) {
                sb.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1)).append(' ');
            }
        }
        return sb.toString().strip();
    }

    private String buscarCoincidenciaCompleta(String normalizado, double tolerancia) {
        String mejor = null;
        int menorDistancia = Integer.MAX_VALUE;
        for (String lugar : lugaresConocidos) {
            String objetivo = FiltroDeRelevancia.normalizar(lugar);
            if (normalizado.isEmpty() || objetivo.isEmpty() || normalizado.charAt(0) != objetivo.charAt(0)) {
                continue;
            }
            int distancia = distanciaLevenshtein(normalizado, objetivo);
            int umbral = Math.max(1, (int) Math.ceil(objetivo.length() * tolerancia));
            if (distancia <= umbral && distancia < menorDistancia) {
                menorDistancia = distancia;
                mejor = lugar;
            }
        }
        return mejor;
    }

    private String buscarCoincidenciaPorVentana(String normalizado, double tolerancia) {
        String mejor = null;
        int menorDistancia = Integer.MAX_VALUE;
        for (String lugar : lugaresConocidos) {
            String objetivo = FiltroDeRelevancia.normalizar(lugar);
            int largo = objetivo.length();
            if (normalizado.length() < largo || largo == 0) {
                continue;
            }
            for (int i = 0; i <= normalizado.length() - largo; i++) {
                String ventana = normalizado.substring(i, i + largo);
                if (ventana.charAt(0) != objetivo.charAt(0)) {
                    continue;
                }
                int distancia = distanciaLevenshtein(ventana, objetivo);
                int umbral = Math.max(1, (int) Math.ceil(largo * tolerancia));
                if (distancia <= umbral && distancia < menorDistancia) {
                    menorDistancia = distancia;
                    mejor = lugar;
                }
            }
        }
        return mejor;
    }

    private static int distanciaLevenshtein(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? dp[i - 1][j - 1]
                        : 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
            }
        }
        return dp[m][n];
    }
}
