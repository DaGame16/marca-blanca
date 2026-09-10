# Flujo — Omnicanal: configuración e ingesta del webhook de LIWA

> Cómo una empresa cliente conecta su WhatsApp/LIWA a la plataforma, y qué pasa
> con cada conversación que LIWA archiva y manda por webhook. Multi-tenant: la
> empresa se resuelve del **secreto** del header, no de un JWT.
> Ver [ADR 0001](../modulos/omnicanal/decisiones/2026-09-10-0001-multi-tenant-y-fin-del-hardcoding.md).

## 1. Panorama

```
A. El operador activa el módulo "omnicanal" para la empresa (consola / aprovisionamiento)
       │
B. El admin del tenant entra a su config       GET  /api/v1/omnicanal/config
       │   -> se crea sola la fila de ruteo (plataforma.tbl_empresas_omnicanal) con un secreto nuevo
       │   -> responde { webhookUrl, webhookSecret, ... }
       │
C. El admin pega webhookUrl + webhookSecret en LIWA, y (opcional)
       PUT  /api/v1/omnicanal/config           { iaHabilitada, openaiModelo, ... }
       PUT  /api/v1/omnicanal/config/liwa-token { token }        (se guarda cifrado)
       │
D. LIWA archiva una conversación y la manda:
       POST /api/v1/omnicanal/webhook/chat-history
            header  x-liwa-webhook-secret: <secret>
            body    { user_id, contact_name, ads, chat_history_details_large, ... }
       │
       ├─ FiltroTenantOmnicanalWebhook: secreto -> empresa -> ContextoEmpresaActual   (401 si no resuelve)
       ├─ InterceptorModuloOmnicanal: 403 si la empresa no tiene el módulo activo
       ▼
       RecibirConversacionArchivadaService.ejecutar(payload)
       │   parsea turnos, deduplica, segmenta en casos, y por cada caso nuevo:
       │   RepositorioAnalisisEscritor.analizarYGuardar -> AnalizadorDeConversacion (OpenAI)
       ▼
       200  { "ok": true }   (siempre 200 si el tenant resolvió; un caso que falla se reintenta luego)
```

Todo lo de `/api/v1/omnicanal/webhook/**` es **público** en `SecurityConfig`.
El resto de `/api/v1/omnicanal/**` exige JWT del tenant y pasa por el
interceptor de módulo.

## 2. Paso A — Activación del módulo

La activación de `omnicanal` para una empresa sale de los flujos ya existentes
(wizard de aprovisionamiento o consola de operación, vía el puente ACL a
`modulos-empresa`). No hay un hook que cree nada de omnicanal en ese momento: la
fila de ruteo se crea sola en el Paso B. Mientras el módulo esté inactivo,
`InterceptorModuloOmnicanal` corta con `403` toda ruta de omnicanal —webhook
incluido—, así que desactivarlo alcanza para frenar la ingesta.

## 3. Paso B — El tenant ve su configuración

`GET /api/v1/omnicanal/config` (JWT del tenant)

```json
{
  "webhookUrl": "https://<base>/api/v1/omnicanal/webhook/chat-history",
  "webhookSecret": "aZ9...sinPadding",
  "iaHabilitada": false,
  "openaiModelo": null,
  "liwaBaseUrl": "https://chat.liwa.co",
  "liwaCustomFieldAds": "587226",
  "liwaTokenConfigurado": false
}
```

- `RegistroRuteoOmnicanalJpa` busca la fila de `plataforma.tbl_empresas_omnicanal`
  por `empresa_id` (resuelto del `identificador` del `ContextoEmpresaActual`). Si
  no existe, la **crea** con un secreto aleatorio (`SecureRandom`, base64url) —
  es justo cuando el tenant lo necesita.
- `webhookUrl` = `app.omnicanal.webhook-url-base` + la ruta fija.
- `liwaBaseUrl` / `liwaCustomFieldAds` salen del perfil (por defecto, los de
  GuajiraNet). El token nunca se devuelve; solo `liwaTokenConfigurado`.

## 4. Paso C — El tenant configura

| Endpoint | Efecto |
|---|---|
| `PUT /api/v1/omnicanal/config` `{ iaHabilitada, openaiModelo, liwaBaseUrl, liwaCustomFieldAds }` | Upsert de la fila única `omnicanal.tbl_configuracion_omnicanal` (base del cliente). Strings vacíos → `null`. |
| `PUT /api/v1/omnicanal/config/liwa-token` `{ token }` | Guarda el token **cifrado** (`CifradorOmnicanal`, AES). Vacío → lo borra. |
| `DELETE /api/v1/omnicanal/config/liwa-token` | Borra el token. |
| `POST /api/v1/omnicanal/config/rotar-secreto` | Nuevo `webhook_secret` en la fila de control; devuelve la vista actualizada. |

El `perfil_analisis` (JSONB, override parcial del perfil ISP: `nombreEmpresa`,
`promptSistema`, `plantillaPrompt`, `lugaresConocidos`, `opcionesMenu`,
`frasesMarcaRuido`, …) todavía no tiene endpoint — se setea por SQL. Cada campo
ausente cae al valor por defecto; JSON inválido → perfil ISP entero.

## 5. Paso D — El webhook entrante

`POST /api/v1/omnicanal/webhook/chat-history`

### 5.1 Resolución de tenant — `FiltroTenantOmnicanalWebhook`

Filtro servlet acotado a `/api/v1/omnicanal/webhook/*` (registrado con
`FilterRegistrationBean`, **no** global). Lee `x-liwa-webhook-secret`, resuelve
`ResolverEmpresaPorWebhookSecreto` (`tbl_empresas_omnicanal` → `empresa_id` →
`identificador`), y:

- secreto ausente / desconocido → escribe `401 {"codigo":401,...}` a mano (un
  filtro no pasa por `@RestControllerAdvice`) y corta.
- ok → `ContextoEmpresaActual.establecer(identificador)`, sigue la cadena, y
  `ContextoEmpresaActual.limpiar()` en un `finally` (el contenedor reusa hilos).

### 5.2 Gate de módulo — `InterceptorModuloOmnicanal`

Interceptor MVC para `/api/v1/omnicanal/**` (corre después de los filtros, con el
tenant ya fijado). `PuenteModulosOmnicanal` (ACL → `ListarModulosDeEmpresa` de
`modulos-empresa`) dice si `omnicanal` está activo para la empresa. Si no →
`403`, el controlador no se ejecuta.

### 5.3 Ingesta — `RecibirConversacionArchivadaService.ejecutar(payload)`

El servicio ya no ve el secreto (lo resolvió el filtro). Con el perfil de la
empresa activa:

1. **Parseo** (`ParseadorDeTurnos`): el texto `NOMBRE (fecha): mensaje`, bloques
   por doble salto de línea, → turnos con autor (`CLIENTE` / `BOT` / `ASESOR`).
2. **Deduplicación**: si el contacto ya tenía conversación, descarta los turnos
   cuya firma `(nombre|fecha|mensaje)` ya está guardada. Si no hay turnos nuevos
   → solo marca `archivada_en` y responde `200`.
3. **Persistencia** de la conversación (`tbl_conversaciones_liwa`, con
   `datos_crudos` = payload completo en JSONB para auditoría) y de los turnos
   nuevos (`tbl_turnos_conversacion_liwa`).
4. **Segmentación** (`DetectorDeCortes`): brecha > 24 h entre turnos ⇒ caso
   nuevo. Por cada segmento con contenido real (`FiltroDeRelevancia` con el
   perfil: descarta menús, encuestas, acuses, saludos, frases-marca) se crea una
   fila en `tbl_casos_liwa`.
5. **Análisis IA** de cada caso nuevo — `RepositorioAnalisisEscritor`:
   - arma los turnos relevantes del caso;
   - `AnalizadorDeConversacion.analizar(...)` → `AdaptadorOpenAI` construye el
     prompt desde `perfil.promptSistema()` / `perfil.plantillaPrompt()`, llama a
     OpenAI con reintentos (429/5xx), y devuelve `{ resultado, modeloUsado }`;
   - normaliza el municipio (`NormalizadorDeMunicipio` con el perfil);
   - calcula abandono, banderas de calidad y métricas de tiempo;
   - guarda en `tbl_conversaciones_analizadas` (1:1 con el caso), con
     `modelo_ia_usado`.
   - Un caso que falla queda con `es_procesada = false` y **no** se propaga (no
     tumba el `200` a LIWA); se reintenta con `GestionarReprocesamiento`.

Si `ia_habilitada` es `false` o no hay `OPENAI_API_KEY`, `AdaptadorOpenAI` lanza
`IllegalStateException` — cae en el `catch` por-caso: la conversación y los
turnos quedan guardados, el análisis no.

## 6. Reprocesamiento

`GestionarReprocesamiento` (endpoints `POST /api/v1/omnicanal/analisis-ia/**`,
JWT del tenant) reintenta casos pendientes o re-analiza por rango de fechas
reusando `RepositorioAnalisisEscritor`. Corre en el contexto de tenant del JWT.

## 7. Módulos backend involucrados

| Módulo | Rol |
|---|---|
| **`omnicanal`** | Todo el flujo: webhook, config self-service, análisis IA, reportes. |
| **`empresas`** | `ContextoEmpresaActual` + enrutamiento multi-tenant (`EnrutadorDataSourcePorEmpresa`). |
| **`modulos-empresa`** | Fuente de verdad de "¿la empresa tiene `omnicanal` activo?" (vía `PuenteModulosOmnicanal`). |
| **`autenticacion`** | `JwtAuthFilter` fija el tenant en las rutas con JWT (`/config`, lectura, reproceso). |

## 8. Datos

**Control (`plataforma`):**

| Changeset | Qué agrega |
|---|---|
| `0024-crear-tabla-empresas-omnicanal` | `tbl_empresas_omnicanal` (`empresa_id` FK, `webhook_secret` UNIQUE) |

**Cliente (schema `omnicanal`):**

| Changeset | Qué agrega |
|---|---|
| `omnicanal/0004..0007` | `tbl_conversaciones_liwa`, `tbl_turnos_conversacion_liwa`, `tbl_casos_liwa`, `tbl_conversaciones_analizadas` |
| `omnicanal/0010-crear-tabla-configuracion-omnicanal` | `tbl_configuracion_omnicanal` (fila única: token cifrado, flags IA, `perfil_analisis` JSONB) |

## 9. Configuración (`application.yml`)

```yaml
app:
  omnicanal:
    clave-maestra:     ${OMNICANAL_CLAVE_MAESTRA:...}         # AES del token de LIWA
    webhook-url-base:  ${OMNICANAL_WEBHOOK_URL_BASE:http://localhost:8080}
    openai-api-key:    ${OMNICANAL_OPENAI_API_KEY:}           # vacío -> IA apagada
    openai-modelo:     ${OMNICANAL_OPENAI_MODELO:gpt-4.1-mini}
```

## 10. Pendientes / DEV-only

- **`EjecutarBackfillDeAds`** sin implementar; `ClienteLiwa.consultarSiVieneDeAds`
  no tiene caller. El `ads` de cada conversación sale hoy solo del payload de LIWA
  (`"ads": "1"`).
- **Editar `perfil_analisis` por API** — hoy solo por SQL.
- **Frontend** (Angular) del self-service de config y de los reportes.
- **`modelo_ia_usado`**: si el operador sobreescribe `app.omnicanal.openai-modelo`
  global y el tenant no fijó el suyo, la columna puede no reflejar el modelo real
  (para arreglarlo, el modelo debería volver siempre desde el adaptador —ya lo
  hace `AnalizadorDeConversacion`, falta usarlo también cuando cae al default).
- **Race** en la creación lazy de la fila de ruteo: dos `GET /config`
  simultáneos de una empresa sin fila → el segundo choca con
  `uq_empresas_omnicanal_empresa`. Raro; el tenant reintenta.
