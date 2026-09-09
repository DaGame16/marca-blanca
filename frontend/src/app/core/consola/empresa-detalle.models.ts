// Espejo de DetalleEmpresaConsola + cuerpos de PUT (backend consola).

export interface ModuloDetalle {
  codigo: string;
  nombre: string;
  activo: boolean;
}

export interface EmpresaDetalle {
  id: string;
  identificador: string;
  nombreLegal: string;
  dominio: string;
  representanteLegal: string;
  correo: string;
  telefono: string;
  sitioWeb: string;
  estado: string;
  colorPrimario: string | null;
  colorSecundario: string | null;
  urlLogo: string | null;
  tipoLogin: number;
  tipoPantallaPrincipal: number;
  modulos: ModuloDetalle[];
}

export interface ActualizarDatosPayload {
  nombreLegal: string;
  representanteLegal: string;
  correo: string;
  telefono: string;
  sitioWeb: string;
}

export interface ActualizarMarcaPayload {
  colorPrimario: string | null;
  colorSecundario: string | null;
  urlLogo: string | null;
  tipoLogin: number;
  tipoPantallaPrincipal: number;
}
