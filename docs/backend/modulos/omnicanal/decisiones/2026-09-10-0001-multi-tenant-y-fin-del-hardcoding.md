# ADR 0001 — Omnicanal multi-tenant y fin del hardcoding de GuajiraNet

**Fecha:** 2026-09-10
**Estado:** Borrador (pendiente de revisión del equipo)
**Módulos afectados:** omnicanal, modulos-empresa, bootstrap (persistencia), autenticacion (referencia)

## Resumen

El módulo `omnicanal` (análisis IA de conversaciones de WhatsApp/LIWA) funciona
hoy solo para GuajiraNet-ISP: la ingesta por webhook no resuelve a qué empresa
pertenece la conversación, el resolver de tenant es un *stub* que siempre lanza
excepción, y el vocabulario de negocio (nombre de la empresa, municipios de La
Guajira, opciones de menú, taxonomía del prompt de IA, campo de atribución de
ads de LIWA) está incrustado en el código —parte de él en `omnicanal-domain`.

Se decide dejarlo multi-tenant en dos movimientos:

1. **Ruteo por secreto.** Una tabla flaca en la base de control
   (`plataforma.tbl_empresas_omnicanal`) que mapea `webhook_secret → empresa`.
   Es lo único que está *obligado* a vivir en la transversal.
2. **Configuración en la base de cada empresa.** Token LIWA, flags de IA, modelo
   y el "perfil de análisis" (municipios, vocab, taxonomía) viven en
   `omnicanal.tbl_configuracion_omnicanal` dentro de la base del cliente, y se
   leen una vez que el contexto de tenant ya está resuelto. El dominio deja de
   contener nombres de lugar y strings de marca.

## Contexto

### Estado actual

- **La persistencia de lectura ya es multi-tenant.** El paquete
  `com.marcablanca.platform.omnicanal.infrastructure.persistencia.cliente` está
  registrado en `ConfiguracionPersistenciaCliente`, así que
  `tbl_conversaciones_liwa`, `tbl_turnos_conversacion_liwa`, `tbl_casos_liwa` y
  `tbl_conversaciones_analizadas` viven en la base de cada empresa y enrutan por
  `ContextoEmpresaActual`. Los endpoints de `OmnicanalController` (con JWT) ya
  caen en la base correcta porque `JwtAuthFilter` fija el contexto.

- **La ingesta por webhook no resuelve tenant.** `OmnicanalWebhookController` es
  `permitAll`, sin JWT, así que `ContextoEmpresaActual` nunca se fija.
  `RecibirConversacionArchivadaService.ejecutar()` llama al resolver de empresa
  pero **descarta el resultado** (usa `.orElseThrow(...)` solo para validar) y
  nunca establece el contexto. Cualquier `INSERT` desde ese flujo revienta con
  `IllegalStateException("No hay empresa activa en el contexto de la peticion")`.

- **El resolver activo es un stub.** `ResolverEmpresaPorWebhookSecretoPendiente`
  es el `@Component` real y siempre lanza `IllegalStateException`. La
  implementación buena (`ResolverEmpresaPorWebhookSecretoJpa` +
  `EmpresaOmnicanalEntity` + `EmpresaRefDeOmnicanal`) ya existe pero no es bean y
  su paquete no está registrado en `ConfiguracionPersistenciaControl`, porque
  `plataforma.tbl_empresas_omnicanal` todavía no existe.

- **Hardcoding de GuajiraNet:**
  | Qué | Dónde | Capa |
  |---|---|---|
  | Prompt de IA: "auditor de calidad para GuajiraNet, un ISP en La Guajira"; taxonomía `motivo_contacto` / `categoria_oficina` de ISP | `AdaptadorOpenAI` (`PROMPT_SISTEMA`, `construirPrompt`) | infraestructura |
  | Lista fija de municipios de La Guajira/Cesar + abreviaturas | `NormalizadorDeMunicipio` | **dominio** |
  | `OPCIONES_MENU` con opciones y municipios de GuajiraNet; regex con `"guajiranet"`, `"guajiranet conectando sueño"`, etc. | `FiltroDeRelevancia` | **dominio** |
  | `CUSTOM_FIELD_ID = "587226"` (campo "VienePautasMeta" de la cuenta LIWA de GuajiraNet) y `https://chat.liwa.co` | `AdaptadorClienteLiwa` | infraestructura |

- **Cabos sueltos relacionados:** `resolverLiwaApiToken` existe en el puerto pero
  nadie lo llama; `EjecutarBackfillDeAds` es un puerto sin implementación; no hay
  chequeo de que la empresa tenga el módulo `omnicanal` activo (otros módulos lo
  hacen vía ACL tipo `PuenteModulosEmpresa`); falta la regla ArchUnit de
  aislamiento `omnicanal.domain` / `.application`; `JsonUtil`, `AdaptadorOpenAI` y
  `AdaptadorClienteLiwa` usan Jackson 2 (`com.fasterxml.jackson.*`) cuando el repo
  va en Jackson 3.

### El problema del huevo y la gallina

LIWA hace `POST /api/v1/omnicanal/webhook/chat-history` con un header de secreto
y nada más: sin JWT, sin subdominio, sin `identificadorEmpresa`. Para enrutar a
la base de la empresa correcta primero hay que saber *cuál* es, y esa búsqueda no
puede vivir en la base de una empresa porque todavía no se sabe en cuál buscar.
Por eso el directorio `secreto → empresa` tiene que estar en
`db_portal_guajiranet_control`. Es el mismo patrón que `plataforma.tbl_empresas`
(identificador → qué base) y `plataforma.tbl_config_correo`.

Nada más de la configuración de omnicanal necesita estar en la transversal: una
vez que el secreto resolvió el tenant y `ContextoEmpresaActual` quedó fijado, el
resto se lee de la base del cliente. El token LIWA ni siquiera se usa en la
ingesta —solo en el backfill de ads, que corre después y con contexto ya puesto.

## Opciones evaluadas

### Dónde vive la configuración por empresa

- **A — Todo en la transversal** (lo que insinuaba el scaffold de
  `EmpresaOmnicanalEntity`, con `liwa_api_token` incluido). Ventaja: resolver
  secreto y token en una sola query. Desventaja: rompe el principio "aislamiento
  total por cliente"; la config y un secreto de un cliente quedan fuera de su
  base; borrar un tenant deja de ser "drop database".
- **B — Todo en la base del cliente.** Imposible: el webhook no puede resolver el
  tenant sin un directorio transversal.
- **C — Híbrida (elegida): directorio mínimo en control, configuración en la base
  del cliente.** La transversal solo guarda `webhook_secret → empresa`. Todo lo
  demás (token, flags de IA, modelo, perfil de análisis) va al schema `omnicanal`
  de la base del cliente y se lee con el contexto ya resuelto.

### Cómo fija el contexto de tenant el webhook

- **Resolver dentro del servicio de aplicación.** Rechazado: obliga a que un
  servicio *framework-free* conozca `ContextoEmpresaActual` (infra) y a manejar
  ahí el `finally`.
- **Filtro servlet dedicado (elegido).** `FiltroTenantOmnicanalWebhook` en
  `omnicanal-infrastructure`, acotado a `/api/v1/omnicanal/webhook/**` con un
  `FilterRegistrationBean` (no global). Resuelve el secreto,
  `ContextoEmpresaActual.establecer(...)` / `finally limpiar()`, y escribe el 401
  JSON directo igual que `JwtAuthFilter` (los filtros no pasan por
  `@RestControllerAdvice`). El controlador queda tonto y el servicio sigue sin
  framework.

### De-hardcodear el dominio

- **Dejar el vocab como constantes "por defecto" y que cada tenant lo
  sobrescriba.** Sirve como paso intermedio pero mantiene nombres de lugar y
  marca dentro de `omnicanal-domain`.
- **Perfil de análisis como value object del dominio (elegido).**
  `PerfilDeAnalisisOmnicanal` con: nombre de empresa, descripción del negocio,
  lugares conocidos, abreviaturas, tokens de menú, frases-marca de ruido,
  marcadores de encuesta, taxonomía de motivos/categorías, criterio de
  "resuelto", `liwaCustomFieldAds`, `liwaBaseUrl`. El dominio conserva solo los
  algoritmos (Levenshtein, segmentación por brecha de 24 h, métricas de tiempo).
  El perfil ISP por defecto (= vocab actual de GuajiraNet, idéntico) vive como
  constante en infraestructura y se siembra como el `perfil_analisis` de la fila
  de GuajiraNet, así su comportamiento no cambia un byte.

## Decisión

### Esquema

**Base de control (`plataforma`) — directorio de ruteo, mínimo:**

```
tbl_empresas_omnicanal
  id, uuid
  empresa_id      FK → plataforma.tbl_empresas   (unique)
  webhook_secret  VARCHAR  UNIQUE                 ← única razón de existir de esta tabla
  creado_en, actualizado_en
```

**Base de cada empresa (schema `omnicanal`) — la configuración real:**

```
tbl_configuracion_omnicanal            (una fila)
  id, uuid
  liwa_api_token         cifrado con CifradorDeCorreo (correo-infrastructure), nullable
  liwa_base_url          nullable → default "https://chat.liwa.co"
  liwa_custom_field_ads  nullable
  ia_habilitada          BOOLEAN default false
  openai_modelo          nullable → default de plataforma (app.omnicanal.openai-modelo)
  perfil_analisis        JSONB nullable → PerfilDeAnalisisOmnicanal.PREDETERMINADO_ISP
  creado_en, actualizado_en
```

### Cambios de código

- **Ruteo:** crear `tbl_empresas_omnicanal` (Liquibase control) + grants;
  registrar `...omnicanal.infrastructure.persistencia.control` en
  `ConfiguracionPersistenciaControl` (`basePackages` + `.packages(...)`) y en
  `@EntityScan` de `MarcaBlancaPlatformApplication`; borrar
  `ResolverEmpresaPorWebhookSecretoPendiente`; poner `@Component` en
  `ResolverEmpresaPorWebhookSecretoJpa` y reescribir `resolverLiwaApiToken` (hoy
  hace dos `findAll()` en memoria) como query directa.
- **Contexto en el webhook:** `FiltroTenantOmnicanalWebhook` +
  `FilterRegistrationBean` acotado. Quitar de
  `RecibirConversacionArchivadaService` el resolve-y-descarta; el filtro es dueño
  de la resolución de tenant, el servicio solo asume "hay empresa activa".
- **Gate de módulo:** puerto de salida `ModuloOmnicanalHabilitado` +
  `PuenteModulosOmnicanal` → in-ports de `modulos-empresa` (patrón
  `PuenteModulosEmpresa`); chequeo en los servicios de `OmnicanalController` y en
  el filtro del webhook. Agregar la regla ArchUnit
  `omnicanal_dominio_y_aplicacion_no_dependen_de_otros_contextos`.
- **Perfil de análisis:** `PerfilDeAnalisisOmnicanal` (dominio) + puerto
  `RepositorioPerfilDeAnalisis#porEmpresaActiva()` que lee el JSONB y cae al
  `PREDETERMINADO_ISP`. `FiltroDeRelevancia` y `NormalizadorDeMunicipio` pasan a
  recibir el perfil; se sacan lugares y marca de `omnicanal-domain`.
  `AdaptadorOpenAI` arma el prompt desde el perfil; `AdaptadorClienteLiwa` toma
  base URL y custom field del perfil. Golden tests: mismas conversaciones de
  muestra → mismo análisis antes/después.
- **Aprovisionar:** al activar el módulo `omnicanal` para una empresa
  (consola/aprovisionamiento) se crea la fila en `tbl_empresas_omnicanal` con
  `webhook_secret` autogenerado, más la fila en `omnicanal.tbl_configuracion_omnicanal`
  de la base del cliente; la consola muestra el secreto para pegarlo en LIWA y
  permite rotarlo y cargar el token LIWA. Implementación real de
  `EjecutarBackfillDeAds` como acción por-tenant.
- **Consistencia:** migrar `JsonUtil`, `AdaptadorOpenAI` y `AdaptadorClienteLiwa`
  a Jackson 3 (`tools.jackson.*`) con mapper inyectado; borrar `JsonUtil`.

### Secuencia de PRs

| PR | Rama | Contenido |
|---|---|---|
| 1 | `feature/omnicanal-tabla-empresas-omnicanal` | Tabla de control flaca + registro de persistencia + resolver JPA activo |
| 2 | `feature/omnicanal-webhook-multitenant` | `FiltroTenantOmnicanalWebhook` + limpieza del servicio + test de integración |
| 3 | `feature/omnicanal-gate-modulo` | ACL `ModuloOmnicanalHabilitado` + `PuenteModulosOmnicanal` + regla ArchUnit |
| 4 | `refactor/omnicanal-perfil-de-analisis` | `PerfilDeAnalisisOmnicanal` + `tbl_configuracion_omnicanal` (schema cliente) + de-hardcodeo dominio/prompt + golden tests |
| 5 | `refactor/omnicanal-jackson-3` | Jackson 3 + mapper inyectado, borrar `JsonUtil` |
| 6 | `feature/omnicanal-aprovisionar-config` | Alta de config al activar el módulo + rotación de secreto + backfill de ads por-tenant |
| 7 | `docs/omnicanal-multi-tenant` | README vivo + este ADR en firme + `docs/backend/flujos/omnicanal-ingesta-webhook.md` |

## Consecuencias

- **Dos escrituras al aprovisionar:** una fila en la base de control y una en la
  base del cliente. Rotar el secreto toca la base de control. A cambio, la
  configuración y los datos de cada empresa quedan en su propia base, consistente
  con "1 base por empresa" (backup, borrar tenant = drop database, aislamiento
  real).
- **El webhook queda con dos puertas:** el filtro resuelve el tenant por el
  secreto y el gate de módulo rechaza la ingesta si la empresa no tiene
  `omnicanal` activo. Un secreto inválido o ausente responde 401.
- **El dominio queda genérico:** después del PR 4, `omnicanal-domain` no menciona
  GuajiraNet, La Guajira ni ningún municipio. Un segundo tenant se configura con
  su propio `perfil_analisis` sin tocar código.
- **Riesgo controlado en el PR 4:** el perfil por defecto reproduce el vocab
  actual exacto; los golden tests garantizan que el análisis de GuajiraNet no
  cambia.
- **`AdaptadorOpenAI`** sigue con una única API key de plataforma (ver preguntas
  abiertas); lo que pasa a ser por-tenant es `ia_habilitada` y el modelo.

## Preguntas abiertas (a resolver con el equipo antes de pasar a "Aceptada")

1. **API key de OpenAI:** ¿una sola key de plataforma (el costo de todos los
   tenants lo absorbe la plataforma) con `ia_habilitada` + modelo por empresa, o
   key propia por tenant? Propuesta: key de plataforma + flags por tenant.
2. **`webhook_secret`:** ¿autogenerado al activar el módulo y mostrado en la
   consola, o lo carga el operador a mano? Propuesta: autogenerado.
3. **Alcance de "omnicanal" para otros tenants:** ¿se asume LIWA/WhatsApp como
   hoy, o hay que dar lugar a otros canales ya en esta fase? Propuesta: asumir
   LIWA y dejar la puerta abierta con una columna `canal`.

## Cómo se podría revertir o evolucionar

- Mientras el ADR esté en borrador y no haya un segundo tenant, revertir es
  borrar `tbl_empresas_omnicanal`, volver a `ResolverEmpresaPorWebhookSecretoPendiente`
  y quitar el registro de persistencia de control.
- Si más adelante se quiere una API key de OpenAI por tenant, se agrega la
  columna cifrada a `tbl_configuracion_omnicanal` sin tocar el ruteo.
- El `perfil_analisis` como JSONB permite versionar el vocab por tenant; si
  crece, puede migrar a su propia tabla en el schema `omnicanal` del cliente sin
  cambiar los puertos.
