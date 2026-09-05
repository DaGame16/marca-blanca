export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  // TODO: reemplazar por el identificador real de la empresa (tabla `empresas`,
  // columna `identificador`) mientras el login no tenga selector de empresa.
  identificadorEmpresaPorDefecto: 'empresa-demo',
  // Clave de administrador (protección interina hasta implementar sistema real de identidad)
  // En producción debe venir de una variable de entorno, nunca hardcodeada
  adminKey: 'solo-para-desarrollo-local-cambiar-siempre',
};
