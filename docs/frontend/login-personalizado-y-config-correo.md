# Login personalizado por subdominio + rediseño de configuración de correo

**Fecha:** 2026-09-09
**Área a cargo de:** Carlos (frontend) — cambios de esta sesión documentados por Neider junto con el backend correspondiente.

Dos cambios de UI sin relación entre sí, hechos en la misma sesión:
personalizar el login cuando el subdominio identifica una empresa, y
rediseñar la pantalla de configuración de correo de la consola de operación.
Se documentan juntos por fecha, no porque compartan código.

## 1. Login: panel de marca personalizado por empresa

`features/auth/login/login.component.ts` resuelve la empresa del subdominio
(`identificadorDesdeSubdominio()`) y le pide su marca al backend
(`GET /api/v1/empresas/{id}/marca`, público). Antes, el panel lateral
(variante `tema-lateral`, la que trae el discurso de venta largo) siempre
mostraba el mismo contenido genérico de la plataforma ("Gestiona tu empresa
desde un solo lugar", bullets de JWT/multi-tenant/escalabilidad), sin
importar qué empresa fuera.

Ahora, si `marcaPublica()?.nombreEmpresa` viene con dato (la empresa fue
identificada por el subdominio), el panel `tema-lateral` cambia a un bloque
propio:

- Logo grande (`.brand-logo-grande`, círculo de 160px, `object-fit: cover`
  para que llene el espacio aunque la imagen no sea cuadrada) en vez del
  logo pequeño genérico.
- El nombre real de la empresa como título, en vez del titular de venta de
  la plataforma.
- Sin el discurso de venta ni los 3 bullets (JWT, multi-tenant, escalable)
  — ese contenido es para vender la plataforma a un cliente nuevo, no tiene
  sentido en el login de una empresa que ya es cliente.

El bloque genérico se conserva intacto en el `@else` — sigue siendo lo que
se ve en el dominio raíz (sin subdominio), donde no hay ninguna empresa que
identificar.

Los otros dos diseños de login (`centrado`, `fondo`) ya mostraban logo +
`nombreEmpresa()` desde antes — no tenían el discurso de venta largo, así
que no necesitaron el mismo cambio. Ahí también se agrandó el logo y se pasó
"Iniciar sesión" a subtítulo, dejando el nombre de la empresa como título
principal (antes era al revés).

**Enlace de registro condicional.** El link "¿Tu empresa aún no tiene cuenta?
Regístrala aquí" (dentro de `formularioTpl`, compartido por los 3 diseños)
ahora se oculta con `@if (!marcaPublica()?.nombreEmpresa)`. Ofrecer registro
a los usuarios que ya están en el login de una empresa existente no tenía
sentido — ese link es para clientes nuevos, que llegan sin subdominio.

## 2. Fuente Sora en toda la aplicación

`src/index.html` cambió el `<link>` de Google Fonts de Inter a Sora
(pesos 400/500/600/700/800), y `src/styles.scss` actualizó `body { font-family: 'Sora', ... }`
y `typography: Sora` en el tema de Angular Material (`mat.theme(...)`) — así
los componentes de Material (botones, inputs) también usan Sora, no solo el
texto plano.

## 3. Rediseño de "Configuración de correo" en la consola

`features/consola/config-correo/consola-config-correo.component.ts`
(`/consola/correo`) es la pantalla real de administración de
`tbl_config_correo` — ver `docs/backend/modulos/correo/` para el backend
detrás. Se rediseñó junto con el cambio de backend que movió la clave SMTP a
la base de datos (ADR 0003 de `correo`), porque el formulario necesitaba un
campo de contraseña real que antes no existía.

Cambios de layout/UX:

- **Formulario agrupado en secciones** (Remitente / Servidor SMTP /
  Credenciales) con un ícono y una etiqueta por sección, en vez de una
  grilla plana de 8 campos sin jerarquía.
- **Campo de contraseña real** (mostrar/ocultar con el ícono de ojo),
  reemplazando el uso indebido que se le venía dando a `secretoRef` (alguien
  había llegado a pegar ahí la clave real, porque no existía un lugar
  correcto para ponerla). Al editar una configuración que ya tiene clave, el
  campo aparece vacío con un texto aclarando que dejarlo así conserva la
  clave actual.
- **Autocompletar servidor SMTP por dominio de correo**: al salir del campo
  "Correo remitente" (evento `blur`), si el dominio es uno conocido (Gmail,
  Outlook/Hotmail/Live, Yahoo, iCloud, Zoho — tabla `SMTP_POR_DOMINIO` en el
  componente), se completan host/puerto/seguridad automáticamente. También
  completa "Usuario" con el mismo correo si está vacío. No pisa un host que
  el admin ya haya escrito a mano (para no romper un Postfix propio, por
  ejemplo).
- **Puerto sugerido al cambiar el tipo de seguridad**: STARTTLS → 587,
  SSL/TLS → 465, sin cifrar → 25. Antes era fácil dejar una combinación
  incompatible (587 + SSL, que en la práctica no conecta) sin ningún aviso.
- **Botón "Guardar" muestra "Verificando conexión…"** mientras el backend
  hace la prueba real de envío antes de persistir (ver ADR 0003 de `correo`)
  — si falla, el mensaje de error del backend (el detalle SMTP real) se
  muestra en un snackbar.
- **Badges por configuración**: `Activa` (ya existía) y `Sin clave` (nuevo —
  avisa si a esa fila le falta contraseña, usando el campo `claveConfigurada`
  que ahora expone el backend en vez del valor real).
- **"Enviar prueba" inline**: cada fila tiene un botón que despliega un
  campo de destinatario + botón "Enviar", con el resultado (éxito o el
  error SMTP real) mostrado ahí mismo, sin salir de la pantalla ni navegar a
  otra vista.

### Pantalla descartada: `/admin/correo`

Durante esta misma sesión se había construido una pantalla standalone
separada (`features/admin/pages/correo-admin/`, protegida por `X-Admin-Key`)
para poder probar el envío de correo mientras la consola de operación no
tenía todavía botón de prueba. Al confirmarse que la consola ya cubre CRUD +
prueba de envío con sesión de operador (más completa y con auditoría), esa
pantalla y su servicio (`core/admin/correo.service.ts`) se **eliminaron** en
la misma sesión — no tiene sentido mantener dos UIs administrando el mismo
recurso con dos mecanismos de auth distintos.
