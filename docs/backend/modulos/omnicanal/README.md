# Módulo `omnicanal`

Ingesta y **análisis de calidad con IA** de las conversaciones de atención al
cliente que llegan por WhatsApp a través de LIWA. Por cada conversación
archivada que LIWA manda por webhook, el módulo la parsea, la corta en "casos"
(tramos con contenido real), y por cada caso llama a un motor de IA que devuelve
motivo de contacto, resultado (`resuelto` / `no_resuelto` / `escalado`),
sentimiento, banderas de calidad, oportunidad de venta, etc. Todo eso alimenta
los reportes de `/api/v1/omnicanal/**`.

Es **multi-tenant**: cada empresa cliente tiene sus propias conversaciones y su
propio "perfil de análisis" (nombre, rubro, municipios de cobertura,
vocabulario, prompt de IA). Ver
[ADR 0001](decisiones/2026-09-10-0001-multi-tenant-y-fin-del-hardcoding.md) y el
[flujo de ingesta](../../flujos/omnicanal-ingesta-webhook.md).

## Qué hace

| Puerto de entrada | Para qué |
|---|---|
| `RecibirConversacionArchivada` | Ingesta del webhook de LIWA: parsea el historial, deduplica turnos ya guardados, segmenta en casos y dispara el análisis IA de cada caso nuevo. La empresa ya viene resuelta en `ContextoEmpresaActual` (la fija `FiltroTenantOmnicanalWebhook`). |
| `ConsultarConversaciones` | Lista conversaciones recientes de un contacto y su resumen. |
| `ConsultarAnalisisDeCasos` | Lista y detalla los análisis IA (con filtros por fecha, resultado, motivo, abandono). |
| `ConsultarReportesOmnicanal` | Agregados: estadísticas, distribución de sentimiento, resumen de soporte / ventas / ads. |
| `GestionarReprocesamiento` | Reintenta o re-analiza casos (pendientes, sin re-analizar, por rango de fechas) y reporta el estado del reproceso. |
| `ConfigurarOmnicanal` | Self-service del tenant: ver la URL/secreto del webhook para pegarlos en LIWA, cargar el token de LIWA, prender el análisis IA, rotar el secreto. |
| `EjecutarBackfillDeAds` | **Sin implementar todavía** — puerto reservado para reconstruir la atribución "viene de ads" consultando la API de LIWA por contacto. |

## Endpoints

| Ruta | Auth | Notas |
|---|---|---|
| `POST /api/v1/omnicanal/webhook/chat-history` | Header `x-liwa-webhook-secret` | Público en `SecurityConfig`. El secreto **es** el dato de tenant. |
| `GET/PUT/POST/DELETE /api/v1/omnicanal/config/**` | JWT del tenant | Self-service de configuración. |
| `GET /api/v1/omnicanal/**` (conversaciones, análisis, estadísticas, reportes) | JWT del tenant | Lectura + reprocesamiento. |

Toda ruta `/api/v1/omnicanal/**` pasa por `InterceptorModuloOmnicanal`: si la
empresa activa no tiene el módulo `omnicanal` activo → `403`.

## Multi-tenancy

- **Ruteo del webhook** (`plataforma.tbl_empresas_omnicanal`, base de **control**):
  única tabla de omnicanal fuera de la base del cliente. Mapea
  `webhook_secret → empresa`. El webhook llega sin JWT ni subdominio, así que la
  resolución de tenant necesita un directorio transversal (mismo rol que
  `tbl_empresa_conexiones`). `FiltroTenantOmnicanalWebhook` la consulta, fija
  `ContextoEmpresaActual` y limpia en un `finally`; secreto inválido → `401`.
- **Configuración por empresa** (`omnicanal.tbl_configuracion_omnicanal`, base de
  **cada cliente**, fila única): token de LIWA (cifrado, `CifradorOmnicanal`),
  `ia_habilitada`, `openai_modelo`, y `perfil_analisis` (JSONB, override parcial
  del perfil por defecto). Sin fila → todo por defecto (perfil ISP, IA apagada,
  sin token).
- **Datos** (`tbl_conversaciones_liwa`, `tbl_turnos_conversacion_liwa`,
  `tbl_casos_liwa`, `tbl_conversaciones_analizadas`, schema `omnicanal` de la
  base del cliente): enrutan por `ContextoEmpresaActual` vía
  `EnrutadorDataSourcePorEmpresa`.
- **Perfil de análisis**: `PerfilDeAnalisisOmnicanal` (dominio) define la forma
  (prompt de IA, datos de LIWA, lugares conocidos, opciones de menú,
  frases-marca de ruido). El perfil ISP por defecto —el vocabulario de
  GuajiraNet, palabra por palabra— vive en `PerfilDeAnalisisPredeterminado`
  (infraestructura), **no** en el dominio. Un tenant nuevo define su perfil sin
  tocar código.
- **API key de OpenAI**: una sola para toda la plataforma
  (`app.omnicanal.openai-api-key`). Por-tenant solo cambian `ia_habilitada` y el
  modelo.

## Estructura

```
backend/omnicanal/
├── omnicanal-domain/          cero framework; solo forma y algoritmos
│   ├── Conversacion, Turno, Caso, AnalisisDeCaso, ResultadoAnalisisIa   ...records de dominio
│   ├── PerfilDeAnalisisOmnicanal          perfil de negocio por empresa (VO)
│   ├── ParseadorDeTurnos                  texto crudo de LIWA -> turnos
│   ├── DetectorDeCortes                   segmenta por brecha de 24 h
│   ├── FiltroDeRelevancia                 ruido vs contenido real -- recibe el perfil
│   └── NormalizadorDeMunicipio            fuzzy-match de lugares -- recibe el perfil
│
├── omnicanal-application/
│   ├── port/in/   RecibirConversacionArchivada, ConsultarConversaciones,
│   │              ConsultarAnalisisDeCasos, ConsultarReportesOmnicanal,
│   │              GestionarReprocesamiento, ConfigurarOmnicanal, EjecutarBackfillDeAds
│   ├── port/out/  AnalizadorDeConversacion (devuelve resultado + modelo usado),
│   │              ClienteLiwa, RepositorioConversaciones/Casos/Analisis,
│   │              ResolverEmpresaPorWebhookSecreto, ModuloOmnicanalHabilitado,
│   │              RepositorioConfiguracionOmnicanal, RegistroRuteoOmnicanal
│   ├── RecibirConversacionArchivadaService   orquesta la ingesta
│   ├── RepositorioAnalisisEscritor           analiza un caso y guarda (compartido ingesta/reproceso)
│   ├── ConfigurarOmnicanalService
│   └── Consultar*/GestionarReprocesamientoService
│
└── omnicanal-infrastructure/
    ├── ConfiguracionOmnicanal            conecta los servicios como beans
    ├── CifradorOmnicanal                 AES para el token de LIWA (espejo de CifradorDeCorreo)
    ├── PerfilDeAnalisisPredeterminado    el perfil ISP por defecto (vocab de GuajiraNet)
    ├── seguridad/AdaptadorOpenAI         único archivo que sabe que existe OpenAI; prompt desde el perfil
    ├── AdaptadorClienteLiwa              único que llama a chat.liwa.co (backfill de ads)
    ├── persistencia/cliente/             conversaciones, turnos, casos, análisis, tbl_configuracion_omnicanal
    ├── persistencia/control/             tbl_empresas_omnicanal: ResolverEmpresaPorWebhookSecretoJpa,
    │                                     RegistroRuteoOmnicanalJpa, PuenteModulosOmnicanal (ACL a modulos-empresa)
    └── web/  OmnicanalController, OmnicanalWebhookController, OmnicanalConfigController,
              FiltroTenantOmnicanalWebhook, InterceptorModuloOmnicanal, ManejadorErroresOmnicanal
```

`omnicanal.infrastructure.persistencia.cliente` está registrado en
`ConfiguracionPersistenciaCliente`; `...persistencia.control` en
`ConfiguracionPersistenciaControl` y en el `@EntityScan` de
`MarcaBlancaPlatformApplication`.

## Configuración (`application.yml`)

```yaml
app:
  omnicanal:
    clave-maestra:      ${OMNICANAL_CLAVE_MAESTRA:...}        # AES del token de LIWA en la base
    webhook-url-base:   ${OMNICANAL_WEBHOOK_URL_BASE:http://localhost:8080}
    openai-api-key:     ${OMNICANAL_OPENAI_API_KEY:}          # vacío -> IA apagada aunque el tenant la prenda
    openai-modelo:      ${OMNICANAL_OPENAI_MODELO:gpt-4.1-mini}
```

## Estado

Funcionando multi-tenant (PRs 1–7 de la migración): ruteo por secreto, gate por
módulo, perfil de análisis por empresa, self-service de configuración, Jackson 3.

Pendiente:

- `EjecutarBackfillDeAds` sin implementar (`ClienteLiwa` no tiene caller).
- Frontend (Angular) del self-service de configuración y de los reportes.
- Desactivar el módulo no borra la fila de ruteo — el `403` del interceptor ya
  frena el webhook.
- `modelo_ia_usado` puede quedar impreciso si un operador sobreescribe
  `app.omnicanal.openai-modelo` global y el tenant no fijó el suyo (columna de
  auditoría; el modelo real debería volver desde `AnalizadorDeConversacion`).

## Historial de cambios

- **2026-09-10** — Migración a multi-tenant: `tbl_empresas_omnicanal` (control),
  `tbl_configuracion_omnicanal` (cliente), `FiltroTenantOmnicanalWebhook`,
  `InterceptorModuloOmnicanal` + `PuenteModulosOmnicanal`,
  `PerfilDeAnalisisOmnicanal` (de-hardcodeo del dominio), `ConfigurarOmnicanal` +
  `OmnicanalConfigController`, Jackson 3. Ver ADR 0001 y el flujo de ingesta.
