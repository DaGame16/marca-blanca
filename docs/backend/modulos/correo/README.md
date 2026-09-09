# Módulo `correo`

Envío de correos transaccionales de la plataforma — hoy, el correo de bienvenida
que recibe una empresa cliente al terminar de aprovisionarse (credenciales +
URL de su sitio nuevo). Incluye también la administración de la configuración
SMTP (`tbl_config_correo`), con la clave cifrada en la propia base de datos y
validación automática de conexión antes de guardar.

## Qué hace

| Puerto de entrada | Para qué |
|---|---|
| `EnviarCorreoDeBienvenida` | Manda el correo de bienvenida (credenciales, URL del sitio). Lo consume `aprovisionamiento`, como último paso del pipeline (`enviarBienvenida`). |
| `EnviarCorreoDeResumenDePago` | Manda un resumen de pago (líneas de factura + total). Construido y con test, **todavía no conectado a ningún flujo real** — el onboarding actual no pasa por una pasarela de pago (ver ADR 0005 de `aprovisionamiento`). Queda listo para cuando eso se agregue. |
| `GestionarConfiguracionCorreo` | CRUD de configuraciones SMTP (crear, actualizar, activar, listar, eliminar). Valida contra un envío real antes de guardar — ver ADR 0003. |
| `ProbarConfiguracionCorreo` | Manda un correo real de prueba: contra una config ya guardada (`ejecutar`) o contra datos que todavía no se guardaron (`ejecutarAdHoc`, usado por la validación automática y por el botón "Enviar prueba" de la consola). |

## Estructura

```
backend/correo/
├── correo-domain/
│   └── .../correo/domain/
│       ├── DireccionCorreo.java              value object -- valida formato, normaliza a minuscula
│       ├── DireccionCorreoInvalidaException.java
│       ├── MensajeDeCorreo.java               lo que finalmente se manda: destinatario + asunto + cuerpo HTML
│       ├── EnvioDeCorreoFallidoException.java
│       ├── ConfiguracionSmtp.java             una fila de tbl_config_correo (SIN la clave -- ver mas abajo)
│       └── ConfiguracionCorreoNoEncontradaException.java
│
├── correo-application/
│   └── .../correo/application/
│       ├── port/in/
│       │   ├── EnviarCorreoDeBienvenida.java
│       │   ├── EnviarCorreoDeResumenDePago.java
│       │   ├── GestionarConfiguracionCorreo.java     ComandoConfiguracionSmtp incluye `clave` (texto plano, solo de entrada)
│       │   └── ProbarConfiguracionCorreo.java         ejecutar(id) / ejecutarAdHoc(DatosConexion)
│       ├── port/out/
│       │   ├── ProveedorDeCorreo.java         unico puerto que sabe que existe un mecanismo real de envio
│       │   ├── RenderizadorDePlantillas.java
│       │   └── RepositorioConfiguracionCorreo.java    obtenerClaveDescifrada(id), buscarActiva(), existeConCorreo(...)
│       ├── EnviarCorreoDeBienvenidaService.java
│       ├── EnviarCorreoDeResumenDePagoService.java
│       └── GestionarConfiguracionCorreoService.java   valida duplicados + dispara la prueba automatica antes de persistir
│
└── correo-infrastructure/
    └── .../correo/infrastructure/
        ├── AdaptadorProveedorCorreoSmtp.java   implementa ProveedorDeCorreo Y ProbarConfiguracionCorreo (misma conexion SMTP, 3 puntos de entrada)
        ├── AdaptadorRenderizadorDePlantillas.java  reemplazo simple de {{clave}}, sin motor de plantillas pesado
        ├── CifradorDeCorreo.java               cifra/descifra la clave SMTP (AES, Spring Security Crypto) antes de tocar la base
        ├── ConfiguracionCorreo.java            conecta los servicios como beans
        ├── persistencia/
        │   ├── ConfiguracionCorreoEntity.java  incluye clave_cifrada (TEXT, nunca se expone)
        │   ├── ConfiguracionCorreoJpaRepository.java
        │   └── RepositorioConfiguracionCorreoJpa.java
        ├── web/
        │   ├── ErrorResponseConfigCorreo.java
        │   └── ManejadorErroresConfigCorreo.java      traduce ConfiguracionCorreoNoEncontradaException (404), IllegalStateException (409) y EnvioDeCorreoFallidoException (502) para cualquier controller de este modulo
        └── resources/plantillas/correo/
            ├── bienvenida.html
            └── resumen-pago.html
```

No hay un controller REST propio en `correo-infrastructure` — el CRUD y el
"probar" se exponen desde la consola de operación (`consola-infrastructure`,
`ConsolaConfigCorreoController`), que solo delega en los puertos de este
módulo. Ver `docs/backend/modulos/consola/` (o el README de `consola` cuando
exista) para la protección por sesión de operador y la auditoría de cada
cambio.

## Cómo se resuelve la configuración SMTP

`AdaptadorProveedorCorreoSmtp.enviar(...)` (envío real, p. ej. bienvenida) lee
la configuración activa (`RepositorioConfiguracionCorreo.buscarActiva()`) y
descifra su clave (`obtenerClaveDescifrada`). Si no hay ninguna fila activa, o
la config exige usuario pero no tiene clave: se loguea una advertencia y se
sale sin lanzar excepción — para no tumbar el pipeline de alta de una empresa
solo porque el correo todavía no está configurado en ese ambiente.

`ejecutar(id, destinatario)` y `ejecutarAdHoc(datos, destinatario)` (los dos
casos de "prueba") **sí propagan la excepción** (`EnvioDeCorreoFallidoException`)
en vez de tragársela — su único propósito es que el error se vea.

## La clave SMTP: cifrada en la base, nunca en variables de entorno

Antes, la única clave SMTP posible para toda la plataforma vivía en una
variable de entorno del backend (`app.aprovisionamiento.smtp-password`) —
cambiarla exigía reiniciar el backend, y solo podía haber una en total.

Hoy cada fila de `tbl_config_correo` tiene su propia clave, guardada
**cifrada** (`clave_cifrada`, columna `TEXT`) con `CifradorDeCorreo` (AES vía
Spring Security Crypto). Un admin puede crear, editar y rotar cuantas
configuraciones quiera desde la consola, sin tocar el backend.

Lo único que sigue siendo una variable de entorno es la **llave maestra de
cifrado** (`app.correo.clave-maestra`, por defecto
`solo-para-desarrollo-local-cambiar-siempre` en dev) — protege lo que hay en
la columna `clave_cifrada`, no es la clave de ningún proveedor SMTP en
particular, y prácticamente nunca cambia. Ver ADR 0003.

`ConfiguracionSmtp` (el record de dominio) **nunca incluye la clave** — solo
un booleano `claveConfigurada`, para que la UI pueda avisar si a una
configuración le falta contraseña sin que el valor real salga jamás del
backend.

## Validación automática al crear/editar (ADR 0003)

`GestionarConfiguracionCorreoService.crear(...)` y `.actualizar(...)` mandan
un correo real de prueba (al propio remitente, vía
`ProbarConfiguracionCorreo.ejecutarAdHoc`) **antes** de tocar la base. Si la
conexión/autenticación SMTP falla, la excepción se propaga y **no se guarda
nada** — evita configuraciones "fantasma" con credenciales que en realidad no
sirven.

En una edición donde el admin no cambia la clave (`clave == null` en el
comando), se resuelve la clave vigente (`obtenerClaveDescifrada`) para poder
validar igual la conexión con los demás campos que sí haya cambiado (host,
puerto, usuario...).

## Reglas de integridad

- **Como máximo una configuración activa a la vez** — índice único parcial
  `uq_config_correo_activa` (`WHERE es_activa`). `activar()` desactiva
  explícitamente cualquier otra fila antes de activar la solicitada, en el
  orden que ese índice exige.
- **No puede haber dos configuraciones con el mismo remitente** —
  `GestionarConfiguracionCorreoService` lo valida antes de guardar
  (`RepositorioConfiguracionCorreo.existeConCorreo`, insensible a
  mayúsculas), reforzado además con el índice único
  `uq_config_correo_remitente` (`lower(remitente_correo)`) por si una
  condición de carrera lo intentara colar.
- `eliminar()` rechaza borrar la configuración activa (`409`) — hay que
  activar otra primero.

## Quién consume este módulo

- `aprovisionamiento` — vía el puerto `EnviarCorreoDeBienvenida`, único punto
  de acoplamiento. `aprovisionamiento` solo conoce el puerto, nunca el
  modelo interno de `correo`.
- `consola` (`ConsolaConfigCorreoController`) — vía `GestionarConfiguracionCorreo`
  y `ProbarConfiguracionCorreo`, para el CRUD y el botón "Enviar prueba" de
  la pantalla `/consola/correo`.

## Pendiente

- La columna `secreto_ref` existe en la tabla (pensada para apuntar a un
  vault real) pero ningún código la lee todavía — sigue siendo un texto
  libre sin resolución real contra ningún vault. Con `clave_cifrada` ya
  resolviendo el problema que motivó esa columna, queda a discusión si
  `secreto_ref` se retira en un futuro changeset.
- `EnviarCorreoDeResumenDePago` queda sin conectar hasta que exista una
  pasarela de pago real en el flujo de onboarding.
- El CRUD no valida formato de correo/host en la entrada más allá de lo que
  la base ya exige (`NOT NULL`) — una validación más fina con
  `jakarta.validation` queda como mejora futura.

## Decisiones de diseño relacionadas

- [`decisiones/2026-09-08-0001-consolidar-envio-en-modulo-correo.md`](decisiones/2026-09-08-0001-consolidar-envio-en-modulo-correo.md)
- [`decisiones/2026-09-08-0002-crud-configuracion-smtp.md`](decisiones/2026-09-08-0002-crud-configuracion-smtp.md)
- [`decisiones/2026-09-09-0003-clave-cifrada-en-bd-y-validacion-automatica.md`](decisiones/2026-09-09-0003-clave-cifrada-en-bd-y-validacion-automatica.md)

---

## Historial de cambios

- **2026-09-08** — Consolidación del envío de correo en este módulo + CRUD
  administrativo de `tbl_config_correo` protegido por `X-Admin-Key`.
- **2026-09-09** — La clave SMTP se mueve de una variable de entorno única a
  `tbl_config_correo.clave_cifrada` (cifrada por fila, administrable sin
  reiniciar el backend). El CRUD se recableó bajo la consola de operación
  (`/api/v1/consola/config-correo`, sesión de operador en vez de
  `X-Admin-Key`) y el viejo `ConfiguracionCorreoAdminController` se retiró.
  Se agregó validación automática de conexión al crear/editar (correo real
  de prueba antes de persistir) y la restricción de remitente único.
