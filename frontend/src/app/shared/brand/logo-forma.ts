/**
 * 1=cuadrado, 2=rectangular, 3=circular -- forma de la caja que contiene el
 * logo (border-radius + aspect-ratio), elegida en "Mi marca" junto con el
 * recorte/posicion de la imagen. Ver backend MarcaDeEmpresa.formaLogo.
 */
export interface EstiloFormaLogo {
  borderRadius: string;
  aspectRatio: string;
}

export function estiloFormaLogo(formaLogo: number | null | undefined): EstiloFormaLogo {
  switch (formaLogo) {
    case 2:
      return { borderRadius: '10px', aspectRatio: '2 / 1' };
    case 3:
      return { borderRadius: '50%', aspectRatio: '1 / 1' };
    default:
      return { borderRadius: '10px', aspectRatio: '1 / 1' };
  }
}
