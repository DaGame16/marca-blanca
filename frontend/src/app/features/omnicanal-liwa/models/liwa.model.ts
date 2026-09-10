// Modelos del módulo Omnicanal (Liwa).
//
// CONTRATO CONFIRMADO por el equipo de backend (2026-09-10) — ver
// documento "Omnicanal (Liwa) — guía de integración para el frontend".
// Ya no hay nada "pendiente de confirmar": las interfaces de la sección
// "Contrato real" de abajo son un calco literal (nombres y casing) de lo
// que el backend Java expone bajo /api/v1/omnicanal/**.
//
// La sección "Modelos de vista" mantiene los mismos nombres de tipo que
// usaban los componentes ya traducidos de guajiranet (LiwaChat,
// LiwaAnalisisItem, etc.) para no tener que reescribir cada componente,
// pero ahora se completan a partir del contrato real vía los adaptadores
// de `data/liwa.service.ts` — ya no son un calco de la API.

// ============================================================================
// Contrato real — tal como lo entrega el backend (interfaces textuales del
// documento de integración). Úsense estos tipos para cualquier llamada HTTP
// nueva.
// ============================================================================

export interface Pagina<T> {
  total: number;
  pagina: number;
  porPagina: number;
  totalPaginas: number;
  items: T[];
}

export interface Conversacion {
  id: number;
  uuid: string;
  idContacto: string;
  nombreContacto: string | null;
  historialChatCompleto: string;
  datosCrudosJson: string;
  esDeAds: boolean;
  creadoEn: string;
  archivadaEn: string | null;
}

export type ResultadoAnalisis = 'RESUELTO' | 'NO_RESUELTO' | 'ESCALADO';
export type AbandonadoPorApi = 'CLIENTE' | 'ASESOR';
export type AutorTurnoApi = 'CLIENTE' | 'ASESOR' | 'BOT';

export interface AnalisisDeCaso {
  id: number;
  uuid: string;
  casoId: number;
  idContacto: string;
  areaDestino: string | null;
  municipio: string | null;
  barrio: string | null;
  categoriaOficina: string | null;
  motivoContacto: string | null;
  submotivo: string | null;
  resumenMotivo: string | null;
  resumenDesenlace: string | null;
  sentimientoInicial: string | null;
  sentimientoFinal: string | null;
  resultado: string | null;
  fcr: boolean | null;
  esfuerzoCliente: string | null;
  temas: string[];
  banderasCalidad: string[];
  oportunidadVenta: boolean | null;
  ventaConfirmadaEnTexto: boolean | null;
  revisarLimite: boolean | null;
  abandono: boolean | null;
  abandonadoPor: string | null;
  esDeAds: boolean;
  modeloIaUsado: string | null;
  razonamiento: string | null;
  cerradoEn: string | null;
  primerMensajeEn: string | null;
  primeraRespuestaEn: string | null;
  procesadoEn: string | null;
}

export interface Turno {
  id: number;
  uuid: string;
  conversacionId: number;
  orden: number;
  autor: string;
  nombreAutor: string | null;
  mensaje: string;
  ocurridoEn: string | null;
}

export interface DetalleAnalisis {
  analisis: AnalisisDeCaso;
  turnosDelCaso: Turno[];
}

export interface Caso {
  id: number;
  uuid: string;
  conversacionId: number;
  turnoOrdenInicio: number;
  turnoOrdenFin: number;
  esProcesada: boolean;
  esDeAds: boolean;
  archivadaEn: string | null;
}

export interface ResumenContacto {
  idContacto: string;
  nombreContacto: string | null;
  totalCasos: number;
  casos: { caso: Caso; analisis: AnalisisDeCaso | null }[];
}

export interface EstadisticasOmnicanal {
  desde: string;
  hasta: string;
  agrupacion: string;
  totalCasos: number;
  serieTemporal: { periodo: string; total: number }[];
}

export interface DistribucionSentimientoOmnicanal {
  sentimientoInicial: Record<string, number>;
  sentimientoFinal: Record<string, number>;
}

export interface ResumenSoporteOmnicanal {
  totalSoporte: number;
  resueltos: number;
  escalados: number;
  noResueltos: number;
}

export interface ResumenVentasOmnicanal {
  totalOportunidades: number;
  confirmadasEnTexto: number;
  tasaConversionTexto: number;
}

export interface ResumenAdsOmnicanal {
  totalCasosDeAds: number;
  porMotivo: Record<string, number>;
  porResultado: Record<string, number>;
}

export interface VistaConfig {
  webhookUrl: string;
  webhookSecret: string;
  iaHabilitada: boolean;
  openaiModelo: string | null;
  liwaBaseUrl: string | null;
  liwaCustomFieldAds: string | null;
  liwaTokenConfigurado: boolean;
}

export interface ActualizarConfigPayload {
  iaHabilitada: boolean;
  openaiModelo: string | null;
  liwaBaseUrl: string | null;
  liwaCustomFieldAds: string | null;
}

export interface ResultadoLote {
  total: number;
  detalles: { casoId: string; exito: boolean; error: string | null }[];
}

export interface ResultadoReproceso {
  total: number;
  exitos: number;
  errores: number;
  casosConError: string[];
}

export interface EstadoReproceso {
  total: number;
  conAnalisisNuevo: number;
  descartadosSinAnalisis: number;
  faltantes: number;
  fallidos: number;
  completo: boolean;
}

export interface ConteoPendientes {
  total: number;
  muestraIds: string[];
}

export interface ResultadoBackfill {
  contactos: number;
  conAds: number;
  sinDato: number;
}

// ============================================================================
// Modelos de vista — mismos nombres que consumían los componentes ya
// traducidos. LiwaService los arma a partir del contrato real de arriba
// (ver adaptadores en data/liwa.service.ts). El casing de los campos tipo
// enum del backend (RESUELTO, CLIENTE, ASESOR…) se normaliza a minúsculas
// acá para no tocar cada componente que ya comparaba contra minúsculas.
// ============================================================================

export type LiwaAutorTurno = 'cliente' | 'bot' | 'asesor';

export interface LiwaMensaje {
  id: string;
  autor: LiwaAutorTurno;
  nombreAutor: string | null;
  texto: string;
  fecha: string;
}

export interface LiwaChat {
  id: string;
  idContacto: string;
  numero: string;
  nombre: string | null;
  agente: string | null;
  /**
   * El contrato real (Conversacion) no trae un flag "procesada" — eso vive
   * en Caso.esProcesada, un nivel más abajo (una conversación puede tener
   * varios casos). Se deja en `false` por defecto; para saber si un
   * contacto ya fue analizado por IA usar el set de idContacto que arma
   * LiwaService.obtenerTodosLosAnalisis() (así lo hacen ya los paneles).
   */
  procesada: boolean;
  mensajes: LiwaMensaje[];
  cantidadMensajes: number;
  archivadaEn: string;
}

export type LiwaSentimientoIA = 'positivo' | 'neutral' | 'negativo';
export type LiwaResultadoIA = 'resuelto' | 'no_resuelto' | 'escalado';
export type LiwaEsfuerzoIA = 'bajo' | 'medio' | 'alto';
export type LiwaAbandonadoPor = 'cliente' | 'asesor';

// motivoContacto es texto libre que devuelve la IA (AnalisisDeCaso.motivoContacto:
// string|null en el contrato real) — ya no es un enum cerrado. Se conserva el
// tipo LiwaMotivoIA solo como llave de MOTIVO_IA_LABELS para los valores
// conocidos; etiquetaMotivo() cae al texto crudo si no está en el mapa.
export type LiwaMotivoIA = 'soporte' | 'facturacion' | 'reconexion' | 'ventas' | 'pqr' | 'cobertura' | 'informacion';

export const MOTIVO_IA_LABELS: Record<LiwaMotivoIA, string> = {
  soporte: 'Soporte',
  facturacion: 'Facturación',
  reconexion: 'Reconexión',
  ventas: 'Ventas',
  pqr: 'PQR',
  cobertura: 'Cobertura',
  informacion: 'Información',
};

export const SENTIMIENTO_LABELS: Record<LiwaSentimientoIA, string> = {
  positivo: 'Positivo',
  neutral: 'Neutral',
  negativo: 'Negativo',
};

export const RESULTADO_LABELS: Record<LiwaResultadoIA, string> = {
  resuelto: 'Resuelto',
  no_resuelto: 'No resuelto',
  escalado: 'Escalado',
};

export const ESFUERZO_LABELS: Record<LiwaEsfuerzoIA, string> = {
  bajo: 'Bajo',
  medio: 'Medio',
  alto: 'Alto',
};

export const ABANDONADO_POR_LABELS: Record<LiwaAbandonadoPor, { label: string; tooltip: string }> = {
  asesor: {
    label: 'Asesor',
    tooltip: 'El lado de la empresa (asesor o bot) dejó de responder o incumplió el siguiente paso.',
  },
  cliente: {
    label: 'Cliente',
    tooltip: 'La empresa respondió y el cliente no volvió a escribir.',
  },
};

export const BANDERAS_CALIDAD_LABELS: Record<string, string> = {
  sin_respuesta: 'Sin respuesta',
  espero_demasiado: 'Esperó demasiado',
  trato_inadecuado: 'Trato inadecuado',
  gestion_pendiente: 'Gestión pendiente',
};

export interface LiwaResumen {
  totalChats: number;
  contactosUnicos: number;
  totalMensajes: number;
  actividadPorDia: { periodo: string; total: number }[];
}

// Vista plana de AnalisisDeCaso, con casing normalizado a minúsculas para
// resultado/abandonadoPor y timestamps re-mapeados a los nombres que ya
// usaban los componentes (tsPrimerMensaje <- primerMensajeEn, etc.).
export interface LiwaAnalisisItem {
  casoId?: string | null;
  archivadaEn?: string | null;
  turnoOrdenInicio?: number | null;
  turnoOrdenFin?: number | null;
  asesor?: string | null;
  asesorId?: string | null;
  ventaConfirmadaEnTexto?: boolean | null;
  id: string;
  idContacto?: string | null;
  resumenMotivo: string;
  resumenDesenlace: string;
  resultado: LiwaResultadoIA | null;
  abandono: boolean;
  abandonadoPor?: LiwaAbandonadoPor | null;
  razonamiento?: string | null;
  sentimientoInicial: string;
  sentimientoFinal: string;
  fcr: boolean;
  esfuerzoCliente: string;
  motivoContacto?: string | null;
  submotivo?: string | null;
  categoriaOficina?: string | null;
  municipio?: string | null;
  barrio?: string | null;
  areaDestino?: string | null;
  tsPrimerMensaje?: string | null;
  tsPrimeraRespuesta?: string | null;
  tsCierre?: string | null;
  temas: string[];
  banderasCalidad: string[];
  oportunidadVenta?: boolean;
  revisarLimite?: boolean;
  modeloIaUsado?: string | null;
  procesadoEn?: string;
  creadoEn?: string;
  fechaAnalisis?: string;
  createdAt?: string;
  // esDeAds en el contrato real es siempre boolean (ya no tri-estado).
  vieneDeAds?: boolean | null;
}

export interface LiwaCasoResumenAnalisis extends LiwaAnalisisItem {}

export interface LiwaCasoDelResumen {
  id: string;
  archivadaEn: string;
  procesada: boolean;
  turnoOrdenInicio: number;
  turnoOrdenFin: number;
  analisis: LiwaCasoResumenAnalisis | null;
}

export interface LiwaResumenContacto {
  idContacto: string;
  nombreContacto: string | null;
  totalCasos: number;
  casos: LiwaCasoDelResumen[];
}

export interface LiwaTurnoAnalizado {
  nombre: string;
  fecha: string;
  mensaje: string;
  autor?: LiwaAutorTurno;
  esCliente?: boolean;
}

export interface LiwaAnalisisDetalle extends LiwaAnalisisItem {
  conversacionCompleta: string;
  conversacionTurnos: LiwaTurnoAnalizado[];
}

export interface LiwaSeriePunto {
  periodo: string;
  total: number;
}

export interface LiwaEstadisticas {
  rango: { desde: string; hasta: string; agrupacion: string };
  totalEventos: number;
  contactosUnicos?: number;
  serieTemporal: LiwaSeriePunto[];
}

export interface LiwaReporteSentimiento {
  sentimientoInicial?: Record<string, number>;
  sentimientoFinal?: Record<string, number>;
  inicial?: Record<string, number>;
  final?: Record<string, number>;
  total?: number;
}

export interface LiwaReporteSoporte {
  total: number;
  resueltos?: number;
  escalados?: number;
  noResueltos?: number;
  porResultado?: Record<string, number>;
  [key: string]: unknown;
}

export interface LiwaReporteVentas {
  totalOportunidades: number;
  ventasConfirmadas?: number;
  oportunidadesConvertidas?: number;
  noConfirmadas?: number;
  tasaConversionTexto: number;
  nota?: string;
  [key: string]: unknown;
}

export interface LiwaReporteAdsMotivo {
  motivo: string | null;
  total: number;
}
export interface LiwaReporteAdsResultado {
  resultado: string | null;
  total: number;
}
export interface LiwaReporteAds {
  rango: { desde?: string; hasta?: string };
  totalCasosDeAds: number;
  porMotivo: LiwaReporteAdsMotivo[];
  porResultado: LiwaReporteAdsResultado[];
}
