export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  // Clave de administrador (protección interina hasta implementar sistema real de identidad)
  // En producción debe venir de una variable de entorno, nunca hardcodeada
  adminKey: 'solo-para-desarrollo-local-cambiar-siempre',
};
