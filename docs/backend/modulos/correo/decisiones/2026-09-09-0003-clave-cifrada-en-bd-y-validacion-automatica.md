# ADR 0003 — Clave SMTP cifrada en `tbl_config_correo` + validación automática al guardar

**Fecha:** 2026-09-09
**Estado:** Aceptada
**Módulos afectados:** correo, consola

## Resumen

La clave SMTP deja de vivir en una única variable de entorno del backend y
pasa a guardarse **cifrada, por fila**, en `tbl_config_correo`. Además, crear
o editar una configuración manda un correo real de prueba antes de guardar
— si falla, no se persiste nada. Se agrega también la restricción de que no
puede haber dos configuraciones con el mismo remitente.

## Contexto

El ADR 0001 de este módulo había dejado la clave en
`app.aprovisionamiento.smtp-password` a propósito, para no romper ninguna
variable ya configurada al mudar el envío desde `aprovisionamiento`. Eso
tenía dos límites que se volvieron un problema real al usar el CRUD del ADR
0002 en el día a día:

- **Solo podía existir UNA clave para toda la plataforma.** El CRUD permite
  guardar varias configuraciones (distintos remitentes/proveedores), pero
  todas terminaban compartiendo la misma variable de entorno — no tenía
  sentido tener varias filas si en la práctica solo una clave podía estar
  vigente.
- **Cambiarla exigía reiniciar el backend.** Un admin no tiene por qué poder
  tocar variables de entorno de un contenedor para rotar una contraseña.

Al usar la pantalla de la consola para configurar un servidor Gmail real, se
detectaron además dos fallas de UX/seguridad concretas: (1) alguien pegó la
contraseña real en el campo `secretoRef` (pensado solo como referencia,
nunca como valor), evidenciando que no había ningún lugar correcto donde
ponerla; y (2) una configuración con puerto/seguridad incompatibles
(587 + `ssl` en vez de `starttls`) se guardó sin ningún aviso, y solo se
notó el error al intentar enviar un correo real de verdad más tarde.

## Decisión

**La clave SMTP se guarda cifrada en la propia tabla**, una por
configuración:

- Columna nueva `tbl_config_correo.clave_cifrada` (`TEXT`, nullable).
- `CifradorDeCorreo` (`correo-infrastructure`) cifra/descifra con AES
  (`Encryptors.text` de Spring Security Crypto), usando una llave maestra
  que **sí sigue viniendo de una variable de entorno**
  (`app.correo.clave-maestra`) — pero esa llave es un secreto de
  *infraestructura* (protege lo que hay en la columna), no la clave de
  ningún proveedor de correo en particular, y prácticamente nunca cambia.
  Rotarla invalidaría todas las claves ya guardadas, así que es un cambio
  deliberado, no una operación de rutina.
- La clave **nunca vuelve a salir del backend**: `ConfiguracionSmtp`
  (dominio) no tiene un campo con la clave, solo `claveConfigurada`
  (booleano) para que la UI pueda avisar si falta. `ComandoConfiguracionSmtp`
  sí la recibe en texto plano al crear/editar (viaja una vez, por HTTPS,
  dentro de una sesión de operador autenticado), pero eso es entrada, no
  salida.
- En una edición, `clave == null` en el comando significa "no cambiarla" —
  el admin no tiene que reescribirla cada vez que ajusta otro campo.

**Se valida contra un envío real antes de guardar.** `GestionarConfiguracionCorreoService.crear`
y `.actualizar` llaman a `ProbarConfiguracionCorreo.ejecutarAdHoc(...)` —
manda un correo de verdad al propio remitente con exactamente los datos que
se están por guardar — **antes** de tocar la base. Si la conexión o la
autenticación fallan, la excepción se propaga (mapeada a `502` con el detalle
del error SMTP) y la fila no se crea/actualiza. Así no quedan
configuraciones "fantasma" con host, usuario o clave que en realidad no
sirven — el error de puerto/seguridad incompatible que motivó este ADR se
habría visto en el momento, no después.

Para que la lógica de conexión no se duplicara entre el envío real y esta
validación ad hoc, `AdaptadorProveedorCorreoSmtp` implementa ahora **tres**
puntos de entrada sobre la misma rutina privada de conexión SMTP:
`ProveedorDeCorreo.enviar` (activa, para pipelines reales),
`ProbarConfiguracionCorreo.ejecutar` (una fila ya guardada, botón "Enviar
prueba") y `ProbarConfiguracionCorreo.ejecutarAdHoc` (datos todavía sin
persistir, usado por la validación automática).

**No puede haber dos configuraciones con el mismo remitente.** Se valida en
`GestionarConfiguracionCorreoService` (insensible a mayúsculas) y se refuerza
con el índice único `uq_config_correo_remitente` sobre
`lower(remitente_correo)`, para que una condición de carrera entre dos
peticiones simultáneas no lo cuele igual.

## Consecuencias

- Se retiró `ConfiguracionCorreoAdminController` (el CRUD original del ADR
  0002, protegido por `X-Admin-Key`) — el CRUD real de correo vive ahora
  exclusivamente en `ConsolaConfigCorreoController`
  (`/api/v1/consola/config-correo`), protegido por sesión de operador y con
  auditoría de cada cambio. Tener el mismo recurso administrable desde dos
  rutas con dos mecanismos de auth distintos era una fuente de divergencia,
  no una capacidad extra.
- `AdaptadorProveedorCorreoSmtp` dejó de depender de `JdbcTemplate`/
  `DataSource` directo — ahora depende de `RepositorioConfiguracionCorreo`
  (puerto de aplicación), que expone `buscarActiva()` y
  `obtenerClaveDescifrada(id)`. Es una dependencia infra→aplicación→infra
  (dos puertos de la misma app se componen en un mismo adaptador), aceptable
  porque ambos puertos pertenecen al mismo módulo.
- `correo-infrastructure` suma `spring-security-crypto` como dependencia.
- Toda configuración creada **antes** de este cambio quedó sin clave
  (`clave_cifrada = NULL`) — hay que volver a escribirla una vez desde la
  consola; no hubo forma de migrar automáticamente un valor que vivía fuera
  de la base.
- La variable de entorno vieja (`app.aprovisionamiento.smtp-password`) ya no
  se lee en ningún lado — se puede retirar de cualquier ambiente donde
  estuviera configurada.
- Como cada validación manda un correo real, crear o editar una
  configuración con datos incorrectos ahora "cuesta" un intento de conexión
  SMTP real (con su latencia) en vez de ser instantáneo — se consideró un
  costo aceptable frente a guardar configuraciones que no sirven.
