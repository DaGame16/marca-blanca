// Coincide con backend: aprovisionamiento-infrastructure/web/RegistrarEmpresaRequest.java
// y RegistrarEmpresaResponse.java (POST /api/v1/admin/empresas).
export interface RegistrarEmpresaRequest {
  identificador: string;
  nombreLegal: string;
  // Campos que el DTO backend actual todavía conserva. La interfaz ya no los
  // muestra, pero se envían como null hasta que el contrato sea migrado.
  nombreComercial: string | null;
  dominio: string | null;
  contrasenaMaestra: string;
  modulosSolicitados: string[];
}

export interface RegistrarEmpresaResponse {
  empresaId: string;
  estado: string;
}
