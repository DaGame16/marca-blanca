// "usuarios" es un modulo base que toda empresa tiene por defecto (no se
// vende ni se activa/desactiva) -- se excluye por nombre, no por una lista
// fija de "los unicos 2 que existen": cualquier modulo nuevo que se agregue
// a tbl_modulos aparece en "Mis modulos"/"Instalar modulos" automaticamente,
// sin tocar estos componentes.
export const CODIGOS_EXCLUIDOS = new Set(['usuarios']);

// Metadatos visuales por módulo (icono + color de acento). El backend solo
// conoce codigo/nombre/descripcion/activo; esto es puramente de presentación.
// Compartido entre "Mis módulos" (instalados) e "Instalar módulos"
// (disponibles) para que un mismo módulo se vea igual en las dos vistas.
const APARIENCIA_MODULO: Record<string, { icono: string; color: string }> = {
  omnicanal: { icono: 'support_agent', color: '#7c3aed' },
  '3cx': { icono: 'call', color: '#2563eb' },
};

const APARIENCIA_DEFECTO = { icono: 'extension', color: '#64748b' };

export function icono(codigo: string): string {
  return (APARIENCIA_MODULO[codigo] ?? APARIENCIA_DEFECTO).icono;
}

export function colorAcento(codigo: string): string {
  return (APARIENCIA_MODULO[codigo] ?? APARIENCIA_DEFECTO).color;
}

export function colorClaro(codigo: string): string {
  return `color-mix(in srgb, ${colorAcento(codigo)} 14%, white)`;
}

export function rutaPanel(codigo: string): string {
  if (codigo === '3cx') {
    return '/panel/pbx-3cx';
  }
  if (codigo === 'omnicanal') {
    return '/panel/omnicanal/liwa';
  }
  return `/panel/${codigo}`;
}
