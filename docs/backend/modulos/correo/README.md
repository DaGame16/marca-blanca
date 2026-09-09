# Módulo `correo`

Envío de correos transaccionales de la plataforma — hoy, el correo de bienvenida
que recibe una empresa cliente al terminar de aprovisionarse (credenciales +
URL de su sitio nuevo). Incluye también la administración de la configuración
SMTP (`tbl_config_correo`) desde la app.

## Qué hace

| Puerto de entrada | Para qué |
|---|---|
| `EnviarCorreoDeBienvenida` | Manda el correo de bienvenida (credenciales, URL del sitio). Lo consume `aprovisionamiento`, como último paso del pipeline (`enviarBienvenida`). |
| `EnviarCorreoDeResumenDePago` | Manda un resumen de pago (líneas de factura + total). Construido y con test, **todavía no conectado a ningún flujo real** — el onboarding actual no pasa por una pasarela de pago (ver ADR 0005 de `aprovisionamiento`). Queda listo para cuando eso se agregue. |
| `GestionarConfiguracionCorreo` | CRUD de la configuración SMTP activa (crear, actualizar, activar, listar, eliminar). Ver sección "Administración" abajo. |

## Estructura

```
backend/correo/
├── correo-domain/
│   └── .../correo/domain/
│       ├── DireccionCorreo.java              value object -- valida formato, normaliza a minuscula
│       ├── DireccionCorreoInvalidaException.java
│       ├── MensajeDeCorreo.java               lo que finalmente se manda: destinatario + asunto + cuerpo HTML
│       ├── EnvioDeCorreoFallidoException.java
│       ├── ConfiguracionSmtp.java             una fila de tbl_config_correo
│       └── ConfiguracionCorreoNoEncontradaException.java
│
├── correo-application/
│   └── .../correo/application/
│       ├── port/in/
│       │   ├── EnviarCorreoDeBienvenida.java
│       │   ├── EnviarCorreoDeResumenDePago.java
│       │   └── GestionarConfiguracionCorreo.java
│       ├── port/out/
│       │   ├── ProveedorDeCorreo.java         unico puerto que sabe que existe un mecanismo real de envio
│       │   ├── RenderizadorDePlantillas.java
│       │   └── RepositorioConfiguracionCorreo.java
│       ├── EnviarCorreoDeBienvenidaService.java
│       ├── EnviarCorreoDeResumenDePagoService.java
│       └── GestionarConfiguracionCorreoService.java
│
└── correo-infrastructure/
    └── .../correo/infrastructure/
        ├── AdaptadorProveedorCorreoSmtp.java   implementa ProveedorDeCorreo, ver seccion siguiente
        ├── AdaptadorRenderizadorDePlantillas.java  reemplazo simple de {{clave}}, sin motor de plantillas pesado
        ├── ConfiguracionCorreo.java            conecta los servicios como beans
        ├── persistencia/
        │   ├── ConfiguracionCorreoEntity.java
        │   ├── ConfiguracionCorreoJpaRepository.java
        │   └── RepositorioConfiguracionCorreoJpa.java
        ├── web/
        │   ├── ConfiguracionCorreoAdminController.java
        │   ├── ConfiguracionCorreoRequest.java
        │   ├── ErrorResponseConfigCorreo.java
        │   └── ManejadorErroresConfigCorreo.java
        └── resources/plantillas/correo/
            ├── bienvenida.html
            └── resumen-pago.html
```

## Cómo se resuelve la configuración SMTP

`AdaptadorProveedorCorreoSmtp` lee la configuración activa (remitente, host,
puerto, usuario, tipo de seguridad) de `plataforma.tbl_config_correo`
(base de **control**), consultando la fila con `es_activa = true` — no de
propiedades estáticas de `application.yml`.

La clave SMTP en sí sigue viniendo de una variable de entorno
(`app.aprovisionamiento.smtp-password`) — el nombre de la propiedad quedó
así a propósito, para no romper ninguna variable ya configurada en algún
ambiente (ver ADR 0001 de este módulo, sección "Consecuencias").

Si no hay ninguna fila activa en `tbl_config_correo`, o falta la clave: se
loguea una advertencia y se sale sin lanzar excepción — para no tumbar el
pipeline de alta de una empresa solo porque el correo todavía no está
configurado en ese ambiente.

## Administración (CRUD de `tbl_config_correo`)

Protegido con `X-Admin-Key` (mismo patrón que `modulos-empresa` — el
interceptor está registrado globalmente para `/api/v1/admin/**`, no hace
falta nada aparte en este módulo).

| Método | Ruta | Qué hace |
|---|---|---|
| `POST` | `/api/v1/admin/correo/config` | Crea una configuración nueva (queda inactiva) |
| `GET` | `/api/v1/admin/correo/config` | Lista todas |
| `GET` | `/api/v1/admin/correo/config/{id}` | Busca una por id |
| `PUT` | `/api/v1/admin/correo/config/{id}` | Actualiza los datos (no cambia si está activa o no) |
| `POST` | `/api/v1/admin/correo/config/{id}/activar` | Activa esta, desactiva cualquier otra activa |
| `DELETE` | `/api/v1/admin/correo/config/{id}` | Borra — rechaza si es la activa (`409`) |

**Regla de negocio, reforzada en 2 niveles:** como máximo una configuración
activa a la vez. La tabla ya tiene un índice único parcial que lo garantiza
a nivel de base; `activar()` respeta el orden correcto (desactivar las demás
antes de activar esta) para no chocar contra ese índice.

## Quién consume este módulo

- `aprovisionamiento` — vía el puerto `EnviarCorreoDeBienvenida`, único punto
  de acoplamiento. `aprovisionamiento` solo conoce el puerto, nunca el
  modelo interno de `correo`.

## Pendiente

- La columna `secreto_ref` existe en la tabla (pensada para apuntar a un
  vault real) pero ningún código la lee todavía — hoy se administra por este
  CRUD como un texto libre, sin resolución real contra ningún vault.
- `EnviarCorreoDeResumenDePago` queda sin conectar hasta que exista una
  pasarela de pago real en el flujo de onboarding.
- El CRUD no valida formato de correo/host en la entrada (`ConfiguracionCorreoRequest`)
  más allá de lo que la base ya exige (`NOT NULL`) — una validación más fina
  con `jakarta.validation` queda como mejora futura, no se agregó ahora para
  no sumar una dependencia sin confirmar que el proyecto ya la trae.
