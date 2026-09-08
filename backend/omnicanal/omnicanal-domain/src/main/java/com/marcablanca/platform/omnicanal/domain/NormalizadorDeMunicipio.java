package com.marcablanca.platform.omnicanal.domain;

import java.util.List;
import java.util.Map;

/** Fuzzy-matching de municipios de La Guajira/Cesar contra lo que devuelve la IA en texto libre. */
public final class NormalizadorDeMunicipio {

    private static final List<String> MUNICIPIOS_CONOCIDOS = List.of(
            "Riohacha", "Albania", "Barrancas", "Dibulla", "Distracción",
            "Fonseca", "Hatonuevo", "La Jagua del Pilar", "Maicao", "Manaure",
            "Molino", "San Juan del Cesar", "Uribia", "Urumita", "Villanueva",
            "Buenavista",
            "Valledupar", "Aguachica", "Agustín Codazzi", "Astrea", "Becerril",
            "Bosconia", "Chimichagua", "Chiriguaná", "Curumaní", "El Copey",
            "El Paso", "Gamarra", "González", "La Gloria", "La Jagua de Ibirico",
            "La Paz", "Manaure Balcón del Cesar", "Pailitas", "Pelaya",
            "Pueblo Bello", "Río de Oro", "San Alberto", "San Diego",
            "San Martín", "Tamalameque", "Guacoche");

    private static final Map<String, String> ABREVIATURAS = Map.of(
            "san juan", "San Juan del Cesar",
            "sanjuan", "San Juan del Cesar",
            "codazzi", "Agustín Codazzi",
            "la jagua", "La Jagua de Ibirico");

    private static final List<String> VACIOS = List.of(
            "null", "n/a", "na", "ninguno", "no aplica", "la guajira", "guajira", "cesar");

    private NormalizadorDeMunicipio() {
    }

    public static String normalizar(String crudo) {
        if (crudo == null) {
            return null;
        }
        String limpio = crudo.strip().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            return null;
        }

        String normalizado = FiltroDeRelevancia.normalizar(limpio);
        if (VACIOS.contains(normalizado)) {
            return null;
        }

        String abreviatura = ABREVIATURAS.get(normalizado);
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

    private static String buscarCoincidenciaCompleta(String normalizado, double tolerancia) {
        String mejor = null;
        int menorDistancia = Integer.MAX_VALUE;
        for (String lugar : MUNICIPIOS_CONOCIDOS) {
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

    private static String buscarCoincidenciaPorVentana(String normalizado, double tolerancia) {
        String mejor = null;
        int menorDistancia = Integer.MAX_VALUE;
        for (String lugar : MUNICIPIOS_CONOCIDOS) {
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
