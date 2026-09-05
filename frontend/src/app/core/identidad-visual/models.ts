// Coincide con backend MarcaRequest/MarcaResponse
// (mismos campos para leer y escribir).
export interface MarcaDeEmpresa {
  urlLogo: string | null;
  colorPrimario: string | null;
  colorSecundario: string | null;
  dominioPropio: string | null;
}
