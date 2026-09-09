// Espejo de EmpresaConsolaResponse (backend consola-infrastructure/web).

export interface EmpresaConsola {
  id: string;
  identificador: string;
  nombreLegal: string;
  dominio: string;
  correo: string;
  estado: string; // borrador | pendiente_aprovisionamiento | activa | suspendida | inactiva
  pasoAprovisionamiento: string | null;
  estadoTarea: string | null;
  creadaEn: string; // ISO-8601
}
