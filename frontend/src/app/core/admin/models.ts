// Modelo del catálogo de módulos disponibles en la plataforma
export interface Modulo {
  id: string;          // UUID
  codigo: string;      // Código único del módulo (ej: "usuarios", "omnicanal")
  nombre: string;      // Nombre para mostrar
  descripcion: string; // Descripción del módulo
}

// Modelo de módulo asociado a una empresa específica con su estado
export interface ModuloDeEmpresa {
  codigo: string;
  nombre: string;
  descripcion: string;
  activo: boolean;     // Si está activado para esta empresa
}

// Modelo de empresa (básico, puede extenderse)
export interface Empresa {
  id: string;          // UUID
  identificador: string;
  nombre: string;
  estado: 'activa' | 'inactiva' | 'suspendida';
}
