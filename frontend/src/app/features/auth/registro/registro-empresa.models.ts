// Coincide con backend: aprovisionamiento-infrastructure/web/RegistrarEmpresaRequest.java
// y RegistrarEmpresaResponse.java (POST /api/v1/admin/empresas).
export interface RegistrarEmpresaRequest {
  identificador: string;
  nombreLegal: string;
  nombreComercial: string | null;
  dominio: string | null;
  contrasenaMaestra: string;
  modulosSolicitados: string[];
}

export interface RegistrarEmpresaResponse {
  empresaId: string;
  estado: string;
}
