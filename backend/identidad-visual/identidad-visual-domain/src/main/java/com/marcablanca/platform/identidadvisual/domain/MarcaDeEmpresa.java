package com.marcablanca.platform.identidadvisual.domain;

/**
 * Los 4 campos son opcionales -- ninguno tiene constraint NOT NULL en la
 * tabla (una empresa puede no haber configurado su marca todavia).
 * Por ahora son 2 colores (primario/secundario) -- confirmado con Luis
 * que un tercero, si llega a hacer falta, requiere una columna nueva en
 * la tabla real primero (coordinar con Leidi antes de agregarlo aca).
 */
public record MarcaDeEmpresa(
        String urlLogo,
        ColorHex colorPrimario,
        ColorHex colorSecundario,
        String dominioPropio,
        // 1..3, igual que en el wizard de registro (aprovisionamiento): que
        // panel de login y que densidad de pantalla usa esta empresa. Antes
        // solo se fijaban una vez durante el registro; ahora tambien se
        // pueden cambiar aqui, self-service, ya con la empresa activa.
        Integer tipoLogin,
        Integer tipoPantallaPrincipal,
        // 1=contener, 2=cubrir, 3=estirar -- como se ajusta urlLogo dentro de
        // su caja donde sea que se muestre (login, sidebar). Ver
        // 0025-agregar-ajuste-logo.yaml.
        Integer ajusteLogo,
        // 1=cuadrado, 2=rectangular, 3=circular -- forma de la caja que
        // contiene el logo (border-radius/aspect-ratio), independiente de
        // ajusteLogo (que solo dice como se ajusta la imagen DENTRO de esa
        // caja). Ver 0026-agregar-forma-logo.yaml.
        Integer formaLogo,
        // Snapshot de solo lectura del primer color no-nulo que tuvo esta
        // empresa (wizard de registro o primer guardado en "Mi marca").
        // Nunca se escribe desde aca -- el cliente no puede mandarlo, lo
        // calcula EmpresaMarcaEntity/MarcaDeAprovisionamientoEntity solos.
        // Ver 0027-agregar-colores-originales.yaml.
        String colorPrimarioOriginal,
        String colorSecundarioOriginal) {
}
