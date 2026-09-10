import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  ActualizarConfigPayload,
  AnalisisDeCaso,
  ConteoPendientes,
  Conversacion,
  DetalleAnalisis,
  DistribucionSentimientoOmnicanal,
  EstadisticasOmnicanal,
  EstadoReproceso,
  LiwaAbandonadoPor,
  LiwaAnalisisDetalle,
  LiwaAnalisisItem,
  LiwaAutorTurno,
  LiwaChat,
  LiwaEstadisticas,
  LiwaMensaje,
  LiwaMotivoIA,
  LiwaResultadoIA,
  LiwaResumenContacto,
  LiwaReporteAds,
  LiwaReporteSentimiento,
  LiwaReporteSoporte,
  LiwaReporteVentas,
  LiwaTurnoAnalizado,
  MOTIVO_IA_LABELS,
  Pagina,
  ResultadoBackfill,
  ResultadoLote,
  ResultadoReproceso,
  ResumenAdsOmnicanal,
  ResumenContacto,
  ResumenSoporteOmnicanal,
  ResumenVentasOmnicanal,
  Turno,
  VistaConfig,
} from '../models/liwa.model';

// ============================================================================
// Contrato CONFIRMADO por el equipo de backend (ver guía de integración
// "Omnicanal (Liwa)"). Todas las rutas cuelgan de environment.apiUrl (que ya
// incluye /api/v1) + /omnicanal/... . La empresa sale del JWT, no se manda
// empresaId en ningún request. 401 sin token, 403 módulo no activo, 404
// análisis/contacto inexistente.
// ============================================================================

const BASE = () => `${environment.apiUrl}/omnicanal`;
const CONFIG_BASE = () => `${environment.apiUrl}/omnicanal/config`;

// Regex de hora embebida en el historial: 2026-08-5 4:10pm / 2026-08-05 16:10
const FECHA_INLINE = /(\d{4})-(\d{1,2})-(\d{1,2})\s+(\d{1,2}):(\d{2})\s*(am|pm)?/i;
// Una línea (turno) viene así:  Autor (fecha hora): texto
const LINEA_MENSAJE = /^(.+?)\s*\(([^)]+)\):\s?([\s\S]*)$/;
// Nombres fijos que usa el flujo automático — igual que NOMBRES_BOT del backend.
const NOMBRES_BOT = new Set(['yo', 'bot']);

function clasificarAutor(nombre: string): LiwaAutorTurno {
  const n = (nombre || '').trim().toLowerCase();
  if (n === 'usuario') return 'cliente';
  if (NOMBRES_BOT.has(n)) return 'bot';
  return 'asesor';
}

function aIso(texto: string): string {
  const m = texto.match(FECHA_INLINE);
  if (!m) return '';
  let hour = parseInt(m[4], 10);
  const ampm = (m[6] || '').toLowerCase();
  if (ampm === 'pm' && hour < 12) hour += 12;
  else if (ampm === 'am' && hour === 12) hour = 0;
  const d = new Date(
    parseInt(m[1], 10),
    parseInt(m[2], 10) - 1,
    parseInt(m[3], 10),
    hour,
    parseInt(m[5], 10),
    0,
  );
  return isNaN(d.getTime()) ? '' : d.toISOString();
}

// Convierte historialChatCompleto (texto plano) en turnos LiwaMensaje[].
export function parsearHistorial(texto: string, fechaPorDefecto?: string): LiwaMensaje[] {
  const mensajes: LiwaMensaje[] = [];
  let fechaActual = fechaPorDefecto || new Date().toISOString();

  for (const lineaVisible of String(texto || '').split(/\r?\n/)) {
    const linea = lineaVisible.trim();
    if (!linea) continue;
    const m = linea.match(LINEA_MENSAJE);
    if (m) {
      const [, nombre, fecha, cuerpo] = m;
      const iso = aIso(fecha) || fechaActual;
      fechaActual = iso;
      mensajes.push({
        id: `liwa-${mensajes.length}-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
        autor: clasificarAutor(nombre),
        nombreAutor: nombre.trim(),
        texto: (cuerpo || '').trim(),
        fecha: iso,
      });
    } else {
      const ultimo = mensajes[mensajes.length - 1];
      if (ultimo) {
        ultimo.texto = `${ultimo.texto}\n${linea}`.trim();
      } else {
        mensajes.push({
          id: `liwa-0-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
          autor: 'cliente',
          nombreAutor: 'Usuario',
          texto: linea,
          fecha: fechaActual,
        });
      }
    }
  }
  return mensajes;
}

// Formatea un número de celular para mostrarlo legible.
export function formatearNumero(numero: string): string {
  const d = String(numero || '').replace(/\D/g, '');
  if (/^57\d{10}$/.test(d)) {
    const p = d.slice(2);
    return `+57 ${p.slice(0, 3)} ${p.slice(3, 6)} ${p.slice(6)}`;
  }
  if (/^3\d{9}$/.test(d)) {
    return `${d.slice(0, 3)} ${d.slice(3, 6)} ${d.slice(6)}`;
  }
  return numero || 'Sin número';
}

function mapearAChat(r: Conversacion): LiwaChat {
  const historial = r.historialChatCompleto || '';
  const archivadaEn = r.archivadaEn || r.creadoEn;
  const mensajes = parsearHistorial(historial, archivadaEn);
  const numero = r.idContacto || 'Sin número';
  const nombre = r.nombreContacto;
  const primerAsesor = mensajes.find((m) => m.autor === 'asesor')?.nombreAutor ?? null;

  return {
    id: r.uuid,
    idContacto: r.idContacto,
    numero,
    nombre,
    agente: primerAsesor,
    // El contrato real no trae "procesada" a nivel de conversación (eso
    // vive en Caso.esProcesada, un nivel más abajo). Ver comentario en
    // models/liwa.model.ts -> LiwaChat.procesada.
    procesada: false,
    mensajes,
    cantidadMensajes: mensajes.length,
    archivadaEn,
  };
}

function normalizarResultado(r: string | null | undefined): LiwaResultadoIA | null {
  if (!r) return null;
  const v = r.toUpperCase();
  if (v === 'RESUELTO') return 'resuelto';
  if (v === 'NO_RESUELTO') return 'no_resuelto';
  if (v === 'ESCALADO') return 'escalado';
  return null;
}

function normalizarAbandonadoPor(v: string | null | undefined): LiwaAbandonadoPor | null {
  if (!v) return null;
  const s = v.toUpperCase();
  if (s === 'CLIENTE') return 'cliente';
  if (s === 'ASESOR') return 'asesor';
  return null;
}

function normalizarAutor(v: string | null | undefined): LiwaAutorTurno {
  const s = (v || '').toUpperCase();
  if (s === 'CLIENTE') return 'cliente';
  if (s === 'BOT') return 'bot';
  return 'asesor';
}

// AnalisisDeCaso (contrato real) -> LiwaAnalisisItem (vista que ya
// consumían los componentes traducidos de guajiranet).
function mapearAAnalisisItem(a: AnalisisDeCaso): LiwaAnalisisItem {
  return {
    id: a.uuid,
    casoId: a.casoId != null ? String(a.casoId) : null,
    idContacto: a.idContacto,
    areaDestino: a.areaDestino,
    municipio: a.municipio,
    barrio: a.barrio,
    categoriaOficina: a.categoriaOficina,
    motivoContacto: a.motivoContacto,
    submotivo: a.submotivo,
    resumenMotivo: a.resumenMotivo ?? '',
    resumenDesenlace: a.resumenDesenlace ?? '',
    sentimientoInicial: a.sentimientoInicial ?? '',
    sentimientoFinal: a.sentimientoFinal ?? '',
    resultado: normalizarResultado(a.resultado),
    fcr: a.fcr ?? false,
    esfuerzoCliente: a.esfuerzoCliente ?? '',
    temas: a.temas ?? [],
    banderasCalidad: a.banderasCalidad ?? [],
    oportunidadVenta: a.oportunidadVenta ?? undefined,
    ventaConfirmadaEnTexto: a.ventaConfirmadaEnTexto,
    revisarLimite: a.revisarLimite ?? undefined,
    abandono: a.abandono ?? false,
    abandonadoPor: normalizarAbandonadoPor(a.abandonadoPor),
    vieneDeAds: a.esDeAds,
    modeloIaUsado: a.modeloIaUsado,
    razonamiento: a.razonamiento,
    tsPrimerMensaje: a.primerMensajeEn,
    tsPrimeraRespuesta: a.primeraRespuestaEn,
    tsCierre: a.cerradoEn,
    procesadoEn: a.procesadoEn ?? undefined,
    archivadaEn: undefined,
  };
}

function mapearATurnoAnalizado(t: Turno): LiwaTurnoAnalizado {
  const autor = normalizarAutor(t.autor);
  return {
    nombre: t.nombreAutor || (autor === 'cliente' ? 'Usuario' : autor === 'bot' ? 'Bot' : 'Asesor'),
    fecha: t.ocurridoEn || '',
    mensaje: t.mensaje,
    autor,
    esCliente: autor === 'cliente',
  };
}

// El filtro de fechas se usa acá SOLO para comparaciones del lado del
// cliente (analisisEnRango / fechaDeItemAnalisis). El backend real espera
// fecha simple "YYYY-MM-DD" (sin hora) en desde/hasta — ya NO se le agrega
// hora antes de mandarlo en el query string (eso era un supuesto del
// backend PHP viejo que no aplica al contrato Java confirmado).
export function rangoConHoras(desde?: string, hasta?: string): { desde?: string; hasta?: string } {
  const fechaSola = /^\d{4}-\d{2}-\d{2}$/;
  return {
    desde: desde && fechaSola.test(desde) ? `${desde}T00:00:00.000` : desde,
    hasta: hasta && fechaSola.test(hasta) ? `${hasta}T23:59:59.999` : hasta,
  };
}

function etiquetaMotivo(motivo: string | null | undefined): string {
  const codigo = (motivo || '').trim();
  if (!codigo) return 'Sin clasificar';
  return MOTIVO_IA_LABELS[codigo as LiwaMotivoIA] || codigo;
}

@Injectable({ providedIn: 'root' })
export class LiwaService {
  private readonly http = inject(HttpClient);

  // ===== Cache corto de recorridos paginados =====
  // Los recorridos que bajan TODAS las páginas de /conversaciones y
  // /analisis son caros. Al montar la vista "Análisis y casos" el mismo
  // recorrido se dispara desde varios paneles a la vez — sin cache eso
  // eran N recorridos completos en paralelo. Este cache corto +
  // deduplicación hace que la primera llamada haga el trabajo y las demás
  // esperen su promesa.
  private readonly CACHE_TTL = 10_000;
  private readonly cache = new Map<string, { creado: number; promesa: Promise<unknown> }>();

  private conCache<T>(clave: string, fabricar: () => Promise<T>): Promise<T> {
    const previo = this.cache.get(clave);
    if (previo && Date.now() - previo.creado < this.CACHE_TTL) return previo.promesa as Promise<T>;
    const promesa = fabricar();
    this.cache.set(clave, { creado: Date.now(), promesa });
    promesa.catch(() => {
      if (this.cache.get(clave)?.promesa === promesa) this.cache.delete(clave);
    });
    return promesa;
  }

  // Invalida el cache corto de /conversaciones y /analisis. Llamar tras un
  // reproceso o reintento de IA (analisis-ia/*) para que la vista siguiente
  // no sirva datos obsoletos.
  invalidarCache(): void {
    this.cache.clear();
  }

  private claveRango(prefijo: string, rango?: { desde?: string; hasta?: string }): string {
    return `${prefijo}|${rango?.desde || ''}|${rango?.hasta || ''}`;
  }

  private async enLotes<T, R>(items: T[], por: number, fn: (item: T) => Promise<R>): Promise<R[]> {
    const resultados: R[] = [];
    for (let i = 0; i < items.length; i += por) {
      resultados.push(...(await Promise.all(items.slice(i, i + por).map(fn))));
    }
    return resultados;
  }

  // ==========================================================================
  // 2. Consultas / reportes — GET /api/v1/omnicanal/**
  // ==========================================================================

  // GET /conversaciones (una página).
  async obtenerConversacionesRecientes(
    params?: { contactId?: string; pagina?: number; porPagina?: number; desde?: string; hasta?: string },
  ): Promise<Conversacion[]> {
    const query: Record<string, string> = {};
    if (params?.contactId) query['contactId'] = params.contactId;
    if (params?.pagina) query['pagina'] = String(params.pagina);
    if (params?.porPagina) query['porPagina'] = String(params.porPagina);
    if (params?.desde) query['desde'] = params.desde;
    if (params?.hasta) query['hasta'] = params.hasta;
    const data = await firstValueFrom(
      this.http.get<Pagina<Conversacion>>(`${BASE()}/conversaciones`, { params: query }),
    );
    return Array.isArray(data.items) ? data.items : [];
  }

  // Recorrido que baja TODAS las páginas de /conversaciones y une los
  // registros, en lotes de a 5 en paralelo.
  private async obtenerTodasLasConversaciones(rango?: { desde?: string; hasta?: string }): Promise<Conversacion[]> {
    const POR_PAGINA = 100;
    const MAX_PAGINAS = 100;
    const query: Record<string, string> = { pagina: '1', porPagina: String(POR_PAGINA) };
    if (rango?.desde) query['desde'] = rango.desde;
    if (rango?.hasta) query['hasta'] = rango.hasta;
    const data1 = await firstValueFrom(
      this.http.get<Pagina<Conversacion>>(`${BASE()}/conversaciones`, { params: query }),
    );

    const todos: Conversacion[] = [];
    const ids = new Set<string>();
    const guardar = (items: Conversacion[]) => {
      const nuevos = items.filter((it) => !ids.has(it.uuid));
      todos.push(...nuevos);
      items.forEach((it) => ids.add(it.uuid));
    };
    guardar(Array.isArray(data1.items) ? data1.items : []);

    const totalPaginas = Math.min(data1.totalPaginas ?? 1, MAX_PAGINAS);
    const total = typeof data1.total === 'number' ? data1.total : Infinity;
    const paginas: number[] = [];
    for (let p = 2; p <= totalPaginas && todos.length < total; p++) paginas.push(p);
    const lotes = await this.enLotes(paginas, 5, async (pagina) => {
      const qq: Record<string, string> = { pagina: String(pagina), porPagina: String(POR_PAGINA) };
      if (rango?.desde) qq['desde'] = rango.desde;
      if (rango?.hasta) qq['hasta'] = rango.hasta;
      const data = await firstValueFrom(
        this.http.get<Pagina<Conversacion>>(`${BASE()}/conversaciones`, { params: qq }),
      );
      return Array.isArray(data.items) ? data.items : [];
    });
    lotes.forEach(guardar);
    return todos;
  }

  // GET /estadisticas — volumen total (totalCasos) + serie temporal.
  async obtenerEstadisticas(
    params?: { agrupacion?: 'dia' | 'semana' | 'mes'; desde?: string; hasta?: string },
  ): Promise<LiwaEstadisticas | null> {
    const query: Record<string, string> = {};
    if (params?.agrupacion) query['agrupacion'] = params.agrupacion;
    if (params?.desde) query['desde'] = params.desde;
    if (params?.hasta) query['hasta'] = params.hasta;
    const data = await firstValueFrom(
      this.http.get<EstadisticasOmnicanal | null>(`${BASE()}/estadisticas`, { params: query }),
    );
    if (!data || typeof data !== 'object') return null;
    return {
      rango: { desde: data.desde, hasta: data.hasta, agrupacion: data.agrupacion },
      totalEventos: data.totalCasos,
      serieTemporal: data.serieTemporal ?? [],
    };
  }

  // Convierte los registros crudos de /conversaciones en LiwaChat[] (mapeo 1:1).
  async obtenerConversacionesChat(rango?: { desde?: string; hasta?: string }): Promise<LiwaChat[]> {
    const clave = this.claveRango('conversaciones', rango);
    return this.conCache(clave, async () => {
      const records = await this.obtenerTodasLasConversaciones(rango);
      return records
        .map(mapearAChat)
        .sort((a, b) => (a.archivadaEn < b.archivadaEn ? 1 : -1));
    });
  }

  // Fecha real de un análisis consolidado.
  fechaDeItemAnalisis(item: LiwaAnalisisItem): Date | null {
    for (const v of [item.tsPrimerMensaje, item.tsPrimeraRespuesta, item.procesadoEn, item.creadoEn, item.fechaAnalisis, item.createdAt]) {
      if (!v) continue;
      const d = new Date(v);
      if (!isNaN(d.getTime())) return d;
    }
    return null;
  }

  analisisEnRango(item: LiwaAnalisisItem, desde?: string, hasta?: string): boolean {
    if (!desde && !hasta) return true;
    const { desde: dI, hasta: hI } = rangoConHoras(desde, hasta);
    const f = this.fechaDeItemAnalisis(item);
    if (!f) return false;
    const ini = dI ? new Date(dI).getTime() : -Infinity;
    const fin = hI ? new Date(hI).getTime() : Infinity;
    const t = f.getTime();
    return t >= ini && t <= fin;
  }

  etiquetaMotivo(motivo: string | null | undefined): string {
    return etiquetaMotivo(motivo);
  }

  tieneBandera(
    item: Pick<LiwaAnalisisItem, 'banderasCalidad'>,
    bandera: 'sin_respuesta' | 'espero_demasiado' | 'trato_inadecuado' | 'gestion_pendiente',
  ): boolean {
    return Array.isArray(item.banderasCalidad) && item.banderasCalidad.includes(bandera);
  }

  // GET /contactos/{idContacto}/resumen
  async obtenerResumenContacto(idContacto: string): Promise<LiwaResumenContacto | null> {
    if (!idContacto) return null;
    try {
      const data = await firstValueFrom(
        this.http.get<ResumenContacto>(`${BASE()}/contactos/${encodeURIComponent(idContacto)}/resumen`),
      );
      if (!data || typeof data !== 'object') return null;
      return {
        idContacto: data.idContacto,
        nombreContacto: data.nombreContacto,
        totalCasos: data.totalCasos,
        casos: (data.casos ?? []).map((c) => ({
          id: c.caso.uuid,
          archivadaEn: c.caso.archivadaEn ?? '',
          procesada: c.caso.esProcesada,
          turnoOrdenInicio: c.caso.turnoOrdenInicio,
          turnoOrdenFin: c.caso.turnoOrdenFin,
          analisis: c.analisis ? mapearAAnalisisItem(c.analisis) : null,
        })),
      };
    } catch {
      return null;
    }
  }

  // GET /analisis — listado paginado de casos analizados por IA.
  async obtenerAnalisis(
    params?: {
      pagina?: number;
      porPagina?: number;
      desde?: string;
      hasta?: string;
      resultado?: string;
      motivoContacto?: string;
      abandono?: boolean;
      abandonadoPor?: 'cliente' | 'asesor';
    },
  ): Promise<{ total: number; pagina: number; porPagina: number; totalPaginas: number; items: LiwaAnalisisItem[] }> {
    const query: Record<string, string> = {};
    if (params?.pagina) query['pagina'] = String(params.pagina);
    if (params?.porPagina) query['porPagina'] = String(params.porPagina);
    if (params?.desde) query['desde'] = params.desde;
    if (params?.hasta) query['hasta'] = params.hasta;
    if (params?.resultado) query['resultado'] = params.resultado;
    if (params?.motivoContacto) query['motivoContacto'] = params.motivoContacto;
    if (params?.abandono != null) query['abandono'] = String(params.abandono);
    if (params?.abandonadoPor) query['abandonadoPor'] = params.abandonadoPor;
    const data = await firstValueFrom(
      this.http.get<Pagina<AnalisisDeCaso>>(`${BASE()}/analisis`, { params: query }),
    );
    return {
      total: data.total,
      pagina: data.pagina,
      porPagina: data.porPagina,
      totalPaginas: data.totalPaginas,
      items: (data.items ?? []).map(mapearAAnalisisItem),
    };
  }

  // Recorrido completo de /analisis (todas las páginas), con cache + lotes.
  async obtenerTodosLosAnalisis(rango?: { desde?: string; hasta?: string }): Promise<LiwaAnalisisItem[]> {
    const clave = this.claveRango('analisis', rango);
    return this.conCache(clave, () => this.recorrerAnalisis(rango));
  }

  private async recorrerAnalisis(rango?: { desde?: string; hasta?: string }): Promise<LiwaAnalisisItem[]> {
    const POR_PAGINA = 100;
    const MAX_PAGINAS = 100;
    const porId = new Map<string, LiwaAnalisisItem>();
    const query: Record<string, string> = { pagina: '1', porPagina: String(POR_PAGINA) };
    if (rango?.desde) query['desde'] = rango.desde;
    if (rango?.hasta) query['hasta'] = rango.hasta;
    const data1 = await firstValueFrom(
      this.http.get<Pagina<AnalisisDeCaso>>(`${BASE()}/analisis`, { params: query }),
    );
    (data1.items ?? []).forEach((it) => {
      const mapeado = mapearAAnalisisItem(it);
      porId.set(mapeado.id, mapeado);
    });

    const totalPaginas = Math.min(data1.totalPaginas ?? 1, MAX_PAGINAS);
    const total = typeof data1.total === 'number' ? data1.total : Infinity;
    const paginas: number[] = [];
    for (let p = 2; p <= totalPaginas && porId.size < total; p++) paginas.push(p);
    const lotes = await this.enLotes(paginas, 5, async (pagina) => {
      const qq: Record<string, string> = { pagina: String(pagina), porPagina: String(POR_PAGINA) };
      if (rango?.desde) qq['desde'] = rango.desde;
      if (rango?.hasta) qq['hasta'] = rango.hasta;
      const data = await firstValueFrom(
        this.http.get<Pagina<AnalisisDeCaso>>(`${BASE()}/analisis`, { params: qq }),
      );
      return (data.items ?? []).map(mapearAAnalisisItem);
    });
    lotes.forEach((items) => items.forEach((it) => porId.set(it.id, it)));
    return Array.from(porId.values());
  }

  // GET /analisis/{id} (id = uuid del análisis) -> DetalleAnalisis, aplanado
  // a la vista LiwaAnalisisDetalle que ya consumía analisis-ia-detalle.
  async obtenerAnalisisDetalle(id: string): Promise<LiwaAnalisisDetalle | null> {
    if (!id) return null;
    try {
      const data = await firstValueFrom(
        this.http.get<DetalleAnalisis>(`${BASE()}/analisis/${encodeURIComponent(id)}`),
      );
      if (!data || typeof data !== 'object' || !data.analisis) return null;
      const turnos = (data.turnosDelCaso ?? []).slice().sort((a, b) => a.orden - b.orden);
      const conversacionCompleta = turnos
        .map((t) => `${t.nombreAutor || t.autor} (${t.ocurridoEn || ''}): ${t.mensaje}`)
        .join('\n');
      return {
        ...mapearAAnalisisItem(data.analisis),
        conversacionCompleta,
        conversacionTurnos: turnos.map(mapearATurnoAnalizado),
      };
    } catch {
      return null;
    }
  }

  private async obtenerReporte<T>(path: string, params?: { desde?: string; hasta?: string }): Promise<T> {
    const query: Record<string, string> = {};
    if (params?.desde) query['desde'] = params.desde;
    if (params?.hasta) query['hasta'] = params.hasta;
    return firstValueFrom(this.http.get<T>(path, { params: query }));
  }

  async obtenerReporteSentimiento(params?: { desde?: string; hasta?: string }): Promise<LiwaReporteSentimiento> {
    const data = await this.obtenerReporte<DistribucionSentimientoOmnicanal>(`${BASE()}/reportes/sentimiento`, params);
    return { sentimientoInicial: data.sentimientoInicial, sentimientoFinal: data.sentimientoFinal };
  }

  async obtenerReporteSoporte(params?: { desde?: string; hasta?: string }): Promise<LiwaReporteSoporte> {
    const data = await this.obtenerReporte<ResumenSoporteOmnicanal>(`${BASE()}/reportes/soporte`, params);
    return {
      total: data.totalSoporte,
      resueltos: data.resueltos,
      escalados: data.escalados,
      noResueltos: data.noResueltos,
    };
  }

  async obtenerReporteVentas(params?: { desde?: string; hasta?: string }): Promise<LiwaReporteVentas> {
    const data = await this.obtenerReporte<ResumenVentasOmnicanal>(`${BASE()}/reportes/ventas`, params);
    return {
      totalOportunidades: data.totalOportunidades,
      ventasConfirmadas: data.confirmadasEnTexto,
      tasaConversionTexto: data.tasaConversionTexto,
    };
  }

  async obtenerReporteAds(params?: { desde?: string; hasta?: string }): Promise<LiwaReporteAds> {
    const data = await this.obtenerReporte<ResumenAdsOmnicanal>(`${BASE()}/reportes/ads`, params);
    const aArray = (r: Record<string, number> | undefined, llave: 'motivo' | 'resultado') =>
      Object.entries(r ?? {}).map(([k, total]) => ({ [llave]: k, total } as any));
    return {
      rango: { desde: params?.desde, hasta: params?.hasta },
      totalCasosDeAds: data.totalCasosDeAds,
      porMotivo: aArray(data.porMotivo, 'motivo'),
      porResultado: aArray(data.porResultado, 'resultado'),
    };
  }

  // ==========================================================================
  // 3. Calidad IA / reproceso — /api/v1/omnicanal/analisis-ia/**
  // ==========================================================================

  async reintentarAnalisis(): Promise<ResultadoLote> {
    const r = await firstValueFrom(this.http.post<ResultadoLote>(`${BASE()}/analisis-ia/reintentar`, {}));
    this.invalidarCache();
    return r;
  }

  async reprocesarTodo(params?: { desde?: string; hasta?: string; soloFaltantes?: boolean }): Promise<ResultadoReproceso> {
    const query: Record<string, string> = {};
    if (params?.desde) query['desde'] = params.desde;
    if (params?.hasta) query['hasta'] = params.hasta;
    if (params?.soloFaltantes != null) query['soloFaltantes'] = String(params.soloFaltantes);
    const r = await firstValueFrom(
      this.http.post<ResultadoReproceso>(`${BASE()}/analisis-ia/reprocesar-todo`, {}, { params: query }),
    );
    this.invalidarCache();
    return r;
  }

  async obtenerEstadoReproceso(params?: { desde?: string; hasta?: string }): Promise<EstadoReproceso> {
    return this.obtenerReporte<EstadoReproceso>(`${BASE()}/analisis-ia/estado-reproceso`, params);
  }

  async obtenerPendientes(params?: { desde?: string; hasta?: string; conIds?: boolean }): Promise<ConteoPendientes> {
    const query: Record<string, string> = {};
    if (params?.desde) query['desde'] = params.desde;
    if (params?.hasta) query['hasta'] = params.hasta;
    if (params?.conIds != null) query['conIds'] = String(params.conIds);
    return firstValueFrom(this.http.get<ConteoPendientes>(`${BASE()}/analisis-ia/pendientes`, { params: query }));
  }

  async backfillAds(): Promise<ResultadoBackfill> {
    const r = await firstValueFrom(this.http.post<ResultadoBackfill>(`${BASE()}/analisis-ia/backfill-ads`, {}));
    this.invalidarCache();
    return r;
  }

  // ==========================================================================
  // 1. Configuración self-service del tenant — /api/v1/omnicanal/config
  // Sirven de base para una futura pantalla de "Configuración" (no
  // construida todavía, ver README/reporte del módulo).
  // ==========================================================================

  async obtenerConfig(): Promise<VistaConfig> {
    return firstValueFrom(this.http.get<VistaConfig>(CONFIG_BASE()));
  }

  async actualizarConfig(payload: ActualizarConfigPayload): Promise<VistaConfig> {
    return firstValueFrom(this.http.put<VistaConfig>(CONFIG_BASE(), payload));
  }

  async guardarTokenLiwa(token: string): Promise<void> {
    await firstValueFrom(this.http.put<void>(`${CONFIG_BASE()}/liwa-token`, { token }));
  }

  async eliminarTokenLiwa(): Promise<void> {
    await firstValueFrom(this.http.delete<void>(`${CONFIG_BASE()}/liwa-token`));
  }

  async rotarSecretoWebhook(): Promise<VistaConfig> {
    return firstValueFrom(this.http.post<VistaConfig>(`${CONFIG_BASE()}/rotar-secreto`, {}));
  }
}
