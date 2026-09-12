// Paleta y estilos compartidos del módulo Liwa -- colores hex EXACTOS del
// original (Next.js/Tailwind) para que los componentes Angular se vean
// idénticos. El proyecto no usa Tailwind (ver docs/frontend), así que estos
// valores se citan literal en el CSS embebido de cada componente; este
// archivo es la referencia única para no repetir/desalinear los mismos
// colores entre componentes (dona, barras, badges, etc.).

// Semántico: mismo criterio en todas las vistas (tablas, badges, gráficas).
export const COLOR_POSITIVO = '#16a34a'; // emerald-600 -- positivo/resuelto/ventas
export const COLOR_POSITIVO_CLARO = '#10b981'; // emerald-500
export const COLOR_NEGATIVO = '#dc2626'; // rose/red-600 -- negativo/no_resuelto/pqr
export const COLOR_NEGATIVO_CLARO = '#ef4444'; // red-500
export const COLOR_NEUTRO = '#64748b'; // slate-500 -- neutro/informacion/abandonado
export const COLOR_ADVERTENCIA = '#f59e0b'; // amber-500 -- escalado/facturacion
export const COLOR_SOPORTE = '#2563eb'; // blue-600 -- soporte
export const COLOR_RECONEXION = '#f97316'; // orange-500
export const COLOR_COBERTURA = '#06b6d4'; // cyan-500
export const COLOR_IA = '#8b5cf6'; // violet-500 -- analisis IA / bot

export const COLOR_RESULTADO: Record<string, string> = {
  resuelto: COLOR_POSITIVO,
  no_resuelto: COLOR_NEGATIVO,
  escalado: COLOR_ADVERTENCIA,
  abandonado: COLOR_NEUTRO,
};

export const COLOR_MOTIVO: Record<string, string> = {
  soporte: COLOR_SOPORTE,
  ventas: COLOR_POSITIVO,
  facturacion: COLOR_ADVERTENCIA,
  reconexion: COLOR_RECONEXION,
  pqr: COLOR_NEGATIVO,
  cobertura: COLOR_COBERTURA,
  informacion: COLOR_NEUTRO,
};

export const COLOR_SENTIMIENTO: Record<string, string> = {
  positivo: COLOR_POSITIVO_CLARO,
  neutral: COLOR_NEUTRO,
  negativo: COLOR_NEGATIVO_CLARO,
};

// Paleta cíclica de la dona "Casos por motivo" (CaseReportsPanel) -- distinta
// a COLOR_MOTIVO a propósito, así era en el original.
export const PALETA_DONA = [
  '#1d4ed8', '#f59e0b', '#10b981', '#ef4444', '#a855f7', '#ec4899', '#6b7280', '#06b6d4',
];

// Tooltip de Chart.js compartido por todas las gráficas del módulo.
export const CHARTJS_TOOLTIP = {
  backgroundColor: '#ffffff',
  titleColor: '#0f172a',
  bodyColor: '#334155',
  borderColor: '#e2e8f0',
  borderWidth: 1,
  cornerRadius: 8,
  padding: 10,
  titleFont: { size: 12, weight: 700 as const },
  bodyFont: { size: 12 },
  boxPadding: 4,
};
