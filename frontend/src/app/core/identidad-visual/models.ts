// Coincide con backend MarcaRequest/MarcaResponse
// (mismos campos para leer y escribir).
export interface MarcaDeEmpresa {
  urlLogo: string | null;
  colorPrimario: string | null;
  colorSecundario: string | null;
  dominioPropio: string | null;
  // 1..3 -- que panel de login (lateral/centrado/fondo) y que diseño de
  // pagina (clasico/derecha/encabezado) usa esta empresa.
  tipoLogin: number | null;
  tipoPantallaPrincipal: number | null;
  // 1=contener (no recorta, puede dejar espacio vacio), 2=cubrir (llena la
  // caja, puede recortar), 3=estirar (llena exacto, puede deformar).
  ajusteLogo: number | null;
  // 1=cuadrado, 2=rectangular, 3=circular -- forma de la caja del logo
  // (border-radius/aspect-ratio), independiente de ajusteLogo.
  formaLogo: number | null;
  // Solo lo llena el endpoint publico (login) -- ver MarcaPublicaController.
  nombreEmpresa?: string | null;
}
