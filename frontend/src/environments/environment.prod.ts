export const environment = {
  production: true,
  apiUrl: '/api/v1',
  // TODO: reemplazar por el identificador real de la empresa (tabla `empresas`,
  // columna `identificador`) mientras el login no tenga selector de empresa.
  identificadorEmpresaPorDefecto: 'empresa-demo',
  // IMPORTANTE: En producción esta clave DEBE configurarse mediante
  // environment variables en el servidor o durante el build.
  // Este valor es un placeholder que debe ser reemplazado en deployment.
  adminKey: 'CAMBIAR_EN_PRODUCCION',
};
