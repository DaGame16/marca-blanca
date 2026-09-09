# Flujo de creación y rediseño empresarial

## Resumen

El frontend de **Marca Blanca** es una aplicación SPA construida con Angular 22, Angular Material, Signals y SCSS. Esta documentación describe el flujo de alta de una empresa, la organización de la experiencia autenticada, la personalización de identidad visual, la selección de temas y el uso del subdominio local durante el desarrollo.

El objetivo del cambio es que una empresa pueda crear su espacio de trabajo mediante un asistente guiado, seleccionar módulos, definir la experiencia visual y acceder posteriormente a su espacio desde un subdominio derivado del identificador.

## Arquitectura frontend

La aplicación mantiene una organización por dominios funcionales. Las rutas públicas se encuentran separadas de las rutas protegidas por autenticación. Las pantallas autenticadas comparten `ShellComponent`, que proporciona navegación lateral, contexto de empresa, perfil, notificaciones y cierre de sesión.

| Área | Ruta | Responsabilidad |
|---|---|---|
| Landing | `/` | Presentación pública de la plataforma y sus soluciones. |
| Acceso | `/login` | Autenticación de la empresa y del usuario. |
| Registro | `/registro` | Asistente de creación de una nueva empresa. |
| Módulos públicos | `/modulos/omnicanal`, `/modulos/pbx-3cx` | Presentación de soluciones vendibles. |
| Módulos contratados | `/mis-modulos` | Activación y desactivación de módulos de la empresa. |
| Identidad | `/mi-marca` | Logo, colores y dominio de marca. |
| Experiencia de acceso | `/tema-login` | Selección del diseño de inicio de sesión. |
| Usuarios | `/usuarios` | Administración de usuarios y accesos. |

Las rutas autenticadas se agrupan bajo `ShellComponent` y utilizan `authGuard`. Las peticiones administrativas utilizan `adminInterceptor` para adjuntar `X-Admin-Key` durante el desarrollo local.

## Asistente de creación

El registro utiliza un flujo de seis estados internos. El usuario puede avanzar y retroceder antes de crear la empresa.

| Paso | Nombre | Contenido |
|---:|---|---|
| 1 | Empresa | Nombre legal, representante legal, correo, teléfono y sitio web. |
| 2 | Módulos | Selección de módulos que estarán disponibles desde el primer día. |
| 3 | Inicio de sesión | Selección visual entre panel lateral, tarjeta centrada y fondo completo. |
| 4 | Páginas | Selección visual entre diseño clásico, compacto y amplio. |
| 5 | Resumen | Revisión de módulos con su costo informativo y activación sin cobro. |
| 6 | Resultado | Confirmación de creación, estado de aprovisionamiento y subdominio local. |

### Datos de empresa

El formulario actual muestra los siguientes campos:

| Campo | Requerido | Uso |
|---|---:|---|
| `nombreLegal` | Sí | Nombre legal de la empresa. |
| `nombreRepresentanteLegal` | Sí | Persona responsable de la empresa. |
| `correo` | Sí | Correo del representante para contacto y notificaciones. |
| `telefono` | Sí | Teléfono del representante. |
| `sitioWeb` | Sí | Sitio web de la empresa. |

El identificador del subdominio lo asigna el servidor a partir del nombre legal; el formulario solo muestra una vista previa normalizada a minúsculas, números y guion bajo, con el sufijo:

```text
identificador.marca-blanca.com
```

En el entorno local, el mismo identificador se transforma en:

```text
http://identificador.localhost:4200/
```

La contraseña inicial tampoco se captura en el wizard: el sistema genera una temporal y la envía por correo, y el primer inicio de sesión obliga a cambiarla — ver [ADR 0007](../backend/modulos/aprovisionamiento/decisiones/2026-09-08-0007-contrasena-temporal-y-primer-login.md).

### Contrato de registro

El paso 1 envía a `POST /api/v1/registro/empresas` los campos que coinciden con `RegistrarEmpresaRequest`:

| Campo enviado | Origen en el formulario |
|---|---|
| `nombreEmpresa` | `nombreLegal` |
| `representanteLegal` | `nombreRepresentanteLegal` |
| `correo` | `correo` |
| `telefono` | `telefono` |
| `sitioWeb` | `sitioWeb` |

El identificador y el dominio no se envían: los asigna el servidor a partir del nombre y vuelven en la respuesta (`identificador`, `dominio`), junto con `empresaId`.

## Catálogo de módulos

El frontend solicita el catálogo mediante `AdminService.getModulos()`. Si la API responde con una lista válida, se utiliza el catálogo recibido. Si responde con una lista vacía o falla durante el desarrollo, se utiliza un catálogo de respaldo para evitar que el usuario quede bloqueado.

El catálogo de respaldo incluye:

| Código | Nombre |
|---|---|
| `usuarios` | Usuarios y acceso |
| `omnicanal` | Comunicación omnicanal |
| `3cx` | 3CX |

El aviso técnico de respaldo no se muestra en la interfaz de usuario. La lógica de fallback es una medida de continuidad local y debe revisarse cuando el catálogo de producción esté siempre disponible.

## Diseño visual

La interfaz pública utiliza una identidad empresarial basada en azul operativo, azul profundo, fondos claros, tipografía Inter y superficies con bordes suaves. La zona autenticada utiliza un sidebar oscuro con navegación agrupada por operación y configuración.

Las tarjetas de selección de temas incluyen previews visuales construidos con HTML y CSS. Los previews responden a los colores y al logo elegidos por el usuario, de modo que la selección no depende únicamente de una descripción textual.

Los diseños de inicio de sesión disponibles son:

- **Panel lateral:** marca a un lado y formulario al otro.
- **Centrado:** tarjeta de acceso centrada.
- **Fondo completo:** fondo degradado con tarjeta flotante.

Los diseños de página disponibles son:

- **Clásico:** navegación lateral y contenido distribuido.
- **Compacto:** barra superior y mayor densidad de información.
- **Amplio:** más espacio entre bloques y tarjetas de mayor tamaño.

La elección se aplica en `ShellComponent` con la clase dinámica `tema-{clasico|compacto|amplio}` sobre `.app-shell`. Al entrar al shell se consulta `MarcaService` para sincronizar el tema desde el backend, de modo que la elección se respete en cualquier navegador y no solo en el que completó el wizard.

## Resumen y creación

El paso final no incluye pasarela de pago. Se muestra el costo de los módulos de forma informativa y la activación se realiza sin cobro. El botón principal es `Crear empresa`.

El alta es pública, sin `X-Admin-Key`: todo `/api/v1/registro/**` está en `permitAll`. Ver [ADR 0005](../backend/modulos/aprovisionamiento/decisiones/2026-09-08-0005-onboarding-publico-sin-pasarela-de-pago.md).

```text
POST /api/v1/registro/empresas
```

```json
{
  "nombreEmpresa": "Mi Empresa S.A.S.",
  "representanteLegal": "Nombre Apellido",
  "correo": "contacto@miempresa.com",
  "telefono": "3001234567",
  "sitioWeb": "https://miempresa.com"
}
```

El backend responde con estado `202 Accepted` y un objeto que contiene el identificador de la empresa y el estado de aprovisionamiento.

## Subdominio local

Al finalizar correctamente, el frontend calcula el subdominio con el protocolo y puerto actuales del navegador:

```ts
`${location.protocol}//${identificador}.localhost:${location.port || '4200'}/`
```

La pantalla de éxito muestra la URL y ofrece dos acciones:

1. **Abrir plataforma**, que abre el subdominio local.
2. **Ir a iniciar sesión**, que abre `/login` dentro del mismo subdominio.

`angular.json` configura el servidor de desarrollo con `host: 0.0.0.0` y `allowedHosts: ["all"]` para aceptar solicitudes realizadas mediante subdominios locales.

> El subdominio `.localhost` funciona para desarrollo. No reemplaza la configuración DNS, proxy reverso o certificado TLS necesarios para un entorno productivo.

## Solución de errores aplicada

Durante la implementación se corrigieron errores de código y de configuración:

| Problema | Corrección |
|---|---|
| Uso de `fullName` no definido en `UserInfo` | Se utiliza `usuarioId` como valor disponible del modelo actual. |
| Conflicto entre rutas raíz | La landing utiliza `pathMatch: 'full'`. |
| `@Service()` inválido en `TareaService` | Se reemplazó por `@Injectable({ providedIn: 'root' })`. |
| Parámetros implícitos en callbacks | Se añadieron tipos explícitos para respuestas y errores. |
| Aviso técnico visible del fallback | Se eliminó de la plantilla. |
| Catálogo vacío | Se agregó catálogo de respaldo. |
| Falta de llaves en `title()` | Se formatearon todos los bloques condicionales. |
| Falta de `rxjs` en `node_modules` | Se mantuvo la dependencia declarada y se documentó la reinstalación local. |

## Ejecución local

Desde la raíz del monorepo:

```bash
cd frontend
pnpm install
pnpm start
```

Si `node_modules` quedó incompleto:

```bash
rm -rf node_modules
pnpm install
pnpm start
```

El frontend se sirve normalmente en `http://localhost:4200/`. Después de crear una empresa con identificador `mi_empresa`, el espacio se puede probar en `http://mi_empresa.localhost:4200/`.

## Archivos principales

| Archivo | Propósito |
|---|---|
| `frontend/src/app/app.routes.ts` | Rutas públicas y autenticadas. |
| `frontend/src/app/layout/shell.component.ts` | Layout empresarial autenticado. |
| `frontend/src/app/features/home/home.component.ts` | Landing pública rediseñada. |
| `frontend/src/app/features/auth/registro/registro-empresa.component.ts` | Asistente completo de creación. |
| `frontend/src/app/features/auth/registro/registro-empresa.models.ts` | Contrato frontend del registro. |
| `frontend/src/app/features/auth/registro/registro-empresa.service.ts` | Cliente HTTP del endpoint de registro. |
| `frontend/angular.json` | Configuración del servidor local y build. |
| `frontend/src/styles.scss` | Tokens y estilos globales. |
| `frontend/tsconfig.json` | Configuración TypeScript. |

## Validación pendiente

La validación final debe ejecutarse en el equipo local después de restaurar `node_modules`. El error de compilación observado durante la sesión se originó porque `rxjs` no estaba presente en la instalación local y porque el montaje FUSE no pudo eliminar correctamente `node_modules/.bin`.

La secuencia recomendada es:

```bash
pnpm install
pnpm build
pnpm start
```

Después se deben comprobar manualmente el registro, la selección de módulos, los previews, la creación de empresa y la apertura del subdominio local.

## Referencias

[1]: https://angular.dev/ "Angular Documentation"
[2]: https://angular.dev/guide/routing "Angular Routing Guide"
[3]: https://angular.dev/guide/components "Angular Components Guide"
