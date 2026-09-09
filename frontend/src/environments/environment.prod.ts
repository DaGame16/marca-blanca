export const environment = {
  production: true,
  apiUrl: '/api/v1',
  // IMPORTANTE: En producción esta clave DEBE configurarse mediante
  // environment variables en el servidor o durante el build.
  // Este valor es un placeholder que debe ser reemplazado en deployment.
  // Coincide con el default de application.yml (ADMIN_CLAVE) para este
  // stack de docker-compose local -- si se despliega esto en un ambiente
  // real hay que sobreescribir ADMIN_CLAVE en el backend Y este valor.
  adminKey: 'solo-para-desarrollo-local-cambiar-siempre',
};
