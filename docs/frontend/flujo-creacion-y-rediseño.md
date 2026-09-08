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
| Tareas | `/tareas` | Gestión de tareas operativas. |

Las rutas autenticadas se agrupan bajo `ShellComponent` y utilizan `authGuard`. Las peticiones administrativas utilizan `adminInterceptor` para adjuntar `X-Admin-Key` durante el desarrollo local.

## Asistente de creación

El registro utiliza un flujo de seis estados internos. El usuario puede avanzar y retroceder antes de crear la empresa.

| Paso | Nombre | Contenido |
|---:|---|---|
| 1 | Empresa | Nombre legal, representante legal, correo, teléfono, identificador y contraseña maestra. |
| 2 | Módulos | Selección de módulos que estarán disponibles desde el primer día. |
| 3 | Inicio de sesión | Selección visual entre panel lateral, tarjeta centrada y fondo completo. |
| 4 | Páginas | Selección visual entre diseño clásico, compacto y amplio. |
| 5 | Resumen | Revisión de módulos, costo de activación y creación gratuita en local. |
| 6 | Resultado | Confirmación de creación, estado de aprovisionamiento y subdominio local. |

### Datos de empresa

El formulario actual muestra los siguientes campos:

| Campo | Requerido | Uso |
|---|---:|---|
| `nombreLegal` | Sí | Nombre legal de la empresa. |
| `nombreRepresentanteLegal` | Sí | Persona responsable de la empresa. |
| `correo` | Sí | Correo del representante para contacto y notificaciones. |
| `telefono` | Sí | Teléfono del representante. |
| `identificador` | Sí | Identificador normalizado utilizado para el subdominio. |
| `contrasenaMaestra` | Sí | Contraseña inicial del primer administrador. |

El identificador se normaliza a minúsculas, números y guion bajo. Se muestra al usuario con el sufijo:

```text
identificador.marca-blanca.com
```

En el entorno local, el mismo identificador se transforma en:

```text
http://identificador.localhost:4200/
```

### Compatibilidad temporal del contrato

El backend actual todavía define `nombreComercial` y `dominio` como campos del `RegistrarEmpresaRequest`. Aunque ya no se muestran en el formulario, el frontend los envía como `null` para mantener compatibilidad con el DTO existente mientras se completa la migración del contrato.

El correo y el teléfono se conservan mediante `DatosContactoPendienteService` hasta que el backend los incorpore formalmente al contrato de aprovisionamiento.

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

## Resumen y creación

El paso final ya no se presenta como una pasarela de pago activa. En el entorno local se muestra como una activación gratuita. El botón principal es `Crear empresa`.

El endpoint utilizado es:

```text
POST /api/v1/admin/empresas
```

El payload compatible con el backend actual es:

```json
{
  "identificador": "mi_empresa",
  "nombreLegal": "Mi Empresa S.A.S.",
  "nombreComercial": null,
  "dominio": null,
  "contrasenaMaestra": "********",
  "modulosSolicitados": ["omnicanal"]
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

Desde PowerShell:

```powershell
cd C:\xampp\htdocs\mi-proyecto-monorepo\marca-blanca\frontend
pnpm install
pnpm start
```

Si `node_modules` quedó incompleto:

```powershell
Remove-Item -Recurse -Force node_modules
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
| `frontend/src/app/features/auth/registro/registro-empresa.service.ts` | Cliente HTTP del endpoint de aprovisionamiento. |
| `frontend/angular.json` | Configuración del servidor local y build. |
| `frontend/src/styles.scss` | Tokens y estilos globales. |
| `frontend/tsconfig.json` | Configuración TypeScript. |

## Validación pendiente

La validación final debe ejecutarse en el equipo local después de restaurar `node_modules`. El error de compilación observado durante la sesión se originó porque `rxjs` no estaba presente en la instalación local y porque el montaje FUSE no pudo eliminar correctamente `node_modules/.bin`.

La secuencia recomendada es:

```powershell
pnpm install
pnpm build
pnpm start
```

Después se deben comprobar manualmente el registro, la selección de módulos, los previews, la creación de empresa y la apertura del subdominio local.

## Referencias

[1]: https://angular.dev/ "Angular Documentation"
[2]: https://angular.dev/guide/routing "Angular Routing Guide"
[3]: https://angular.dev/guide/components "Angular Components Guide"
