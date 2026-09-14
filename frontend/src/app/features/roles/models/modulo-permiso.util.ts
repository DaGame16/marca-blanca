import { Permiso } from './permiso.model';

// Nombre de permiso = "modulo:accion" (ej. "usuarios:crear"). Este mapa es
// solo para mostrar un titulo legible por seccion en la UI -- si aparece un
// modulo nuevo que todavia no esta aca, se muestra su codigo tal cual (nunca
// se rompe, solo se ve menos lindo hasta que se agregue la traduccion).
const TITULOS_DE_MODULO: Record<string, string> = {
  usuarios: 'Usuarios',
  roles: 'Roles y permisos',
  marca: 'Identidad de marca',
  modulos: 'Módulos de la empresa',
  omnicanal: 'Omnicanal',
};

export interface GrupoDePermisos {
  modulo: string;
  tituloModulo: string;
  permisos: Permiso[];
}

export function moduloDe(permiso: Permiso): string {
  return permiso.nombre.split(':')[0];
}

export function tituloDeModulo(modulo: string): string {
  return TITULOS_DE_MODULO[modulo] ?? modulo;
}

/** Agrupa el catalogo por modulo, cada grupo ordenado por titulo. */
export function agruparPermisosPorModulo(permisos: Permiso[]): GrupoDePermisos[] {
  const grupos = new Map<string, Permiso[]>();
  for (const permiso of permisos) {
    const modulo = moduloDe(permiso);
    const lista = grupos.get(modulo) ?? [];
    lista.push(permiso);
    grupos.set(modulo, lista);
  }
  return Array.from(grupos.entries())
    .map(([modulo, permisos]) => ({ modulo, tituloModulo: tituloDeModulo(modulo), permisos }))
    .sort((a, b) => a.tituloModulo.localeCompare(b.tituloModulo));
}
