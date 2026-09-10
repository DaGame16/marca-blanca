import { LiwaChat, LiwaResumen } from '../models/liwa.model';

// Traducción 1:1 de lib/liwa-resumen.ts (guajiranet).
export function generarResumen(chats: LiwaChat[]): LiwaResumen {
  // idContacto es el identificador confiable (ya viene de LIWA) — antes se
  // usaba c.numero, pero ese sale de un texto extraído/heurístico.
  const contactosUnicos = new Set(chats.map((c) => c.idContacto)).size;
  const totalMensajes = chats.reduce((s, c) => s + c.cantidadMensajes, 0);

  // Actividad por día — se cuenta directo de las conversaciones reales
  // (archivadaEn), no de un agregado del backend, para que la gráfica
  // siempre cuadre con la tabla/KPIs.
  const porDia = new Map<string, number>();
  chats.forEach((c) => {
    const d = new Date(c.archivadaEn);
    if (isNaN(d.getTime())) return;
    // OJO: nada de .toISOString() — convierte a UTC antes de formatear, y
    // Colombia (UTC-5) puede quedar "un día adelante" en UTC. Se arma el
    // día con los componentes LOCALES.
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const dia = `${y}-${m}-${dd}`;
    porDia.set(dia, (porDia.get(dia) || 0) + c.cantidadMensajes);
  });
  const actividadPorDia = Array.from(porDia, ([dia, total]) => ({ dia, total }))
    .sort((a, b) => (a.dia < b.dia ? -1 : 1))
    .map((p) => ({
      periodo: new Date(`${p.dia}T00:00:00`).toLocaleDateString('es-CO', { day: '2-digit', month: 'short' }),
      total: p.total,
    }));

  return {
    totalChats: chats.length,
    contactosUnicos,
    totalMensajes,
    actividadPorDia,
  };
}
