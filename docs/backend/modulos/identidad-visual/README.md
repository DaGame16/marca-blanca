# Módulo Identidad Visual

> Resumen vivo. Se actualiza *in place* cada vez que el módulo cambia.

## 1. Qué es y qué NO es

**Marca blanca visual: logo, colores (primario/secundario), dominio propio — configurado por el propio cliente sobre su propia empresa.** Self-service, protegido por JWT normal — no por la clave de administrador de plataforma que usa `empresas`.

Deliberadamente separado del módulo `empresas` (ver [ADR 0001](decisiones/2026-09-04-0001-modulo-separado-no-extension-de-empresas.md)) — modelo de acceso distinto, no solo un dato más en la misma base.

**NO le corresponde (todavía):** subir el logo en sí (solo se guarda su URL, subido en otro lado), verificar DNS o provisionar certificado para el dominio propio, ni un tercer color (requiere columna nueva en la tabla real, coordinar con Leidi primero).

## 2. Estructura de paquetes

```
identidad-visual/
├── identidad-visual-domain/.../identidadvisual/domain/
│   ├── ColorHex.java
│   ├── MarcaDeEmpresa.java
│   └── EmpresaNoEncontradaException.java
├── identidad-visual-application/.../identidadvisual/application/
│   ├── ObtenerMarcaDeEmpresaService.java, ActualizarMarcaDeEmpresaService.java
│   └── port/
│       ├── in/ObtenerMarcaDeEmpresa.java, ActualizarMarcaDeEmpresa.java
│       └── out/RepositorioMarcaDeEmpresa.java
└── identidad-visual-infrastructure/.../identidadvisual/infrastructure/
    ├── EmpresaRefEntity.java   <- lectura minima de tbl_empresas, sin depender de "empresas"
    ├── EmpresaMarcaEntity.java, EmpresaMarcaJpaRepository.java
    ├── RepositorioMarcaDeEmpresaJpa.java, ConfiguracionIdentidadVisual.java
    └── web/
        ├── MarcaController.java
        ├── MarcaRequest.java, MarcaResponse.java
        ├── ErrorResponse.java, ManejadorErroresIdentidadVisual.java
```

## 3. Dominio (`identidad-visual-domain`)

- **`ColorHex`**: value object, valida formato hexadecimal (`#RRGGBB`), normaliza a mayúsculas.
- **`MarcaDeEmpresa`**: `record(String urlLogo, ColorHex colorPrimario, ColorHex colorSecundario, String dominioPropio)` — los 4 campos son opcionales (ninguno tiene `NOT NULL` en la tabla real).
- **`EmpresaNoEncontradaException`**.

## 4. Aplicación (`identidad-visual-application`)

- **`ObtenerMarcaDeEmpresa`**: dado el identificador de empresa, devuelve su marca — si nunca configuró nada, devuelve `MarcaDeEmpresa` con los 4 campos en `null` (no es un error, es un estado válido).
- **`ActualizarMarcaDeEmpresa`**: reemplazo completo (no parcial) de los 4 campos.

Ambas identifican la empresa por su **`identificador`** (slug), no por UUID — es el único dato disponible en toda la cadena de autenticación (ver ADR 0001).

## 5. Infraestructura (`identidad-visual-infrastructure`)

### Persistencia
- **`EmpresaRefEntity`**: mapeo de solo lectura de `tbl_empresas`, con lo mínimo (`id`, `uuid`, `identificador`, `estado`) — a propósito no reutiliza `EmpresaEntity` del módulo `empresas`, para no acoplar los dos módulos en código.
- **`EmpresaMarcaEntity`**: mapeo de `tbl_empresas_marca`.
- Vive en la unidad de persistencia **"control"** (`ConfiguracionPersistenciaControl`, en `bootstrap`), junto a `empresas.infrastructure` — ambos paquetes están en su `@EntityScan`/`@EnableJpaRepositories`.

### `web/` — self-service, sin clave de administrador
`MarcaController` saca el `identificadorEmpresa` del **atributo del request que deja `JwtAuthFilter`** (ver README de `autenticacion`), nunca de un parámetro de la URL o del body — así ningún usuario puede tocar la marca de una empresa que no es la suya, sin importar qué le pida al servidor. No hace falta ningún ajuste en `SecurityConfig`: la ruta ya exige JWT por defecto (`.anyRequest().authenticated()`).

## 6. Contrato REST

| Método | Ruta | Protección | Body | Respuesta OK | Errores |
|---|---|---|---|---|---|
| GET | `/api/v1/mi-empresa/marca` | JWT | — | 200 + `MarcaResponse` | 401 |
| PUT | `/api/v1/mi-empresa/marca` | JWT | `MarcaRequest` | 204 | 400 (color inválido) · 401 |

## 7. Decisiones de diseño relacionadas

- [`decisiones/2026-09-04-0001-modulo-separado-no-extension-de-empresas.md`](decisiones/2026-09-04-0001-modulo-separado-no-extension-de-empresas.md)
- [`autenticacion/decisiones/2026-09-04-0005-jwt-lleva-empresa-como-claim.md`](../autenticacion/decisiones/2026-09-04-0005-jwt-lleva-empresa-como-claim.md) — de donde sale el `identificadorEmpresa` que usa este módulo.

## 8. Pendiente / próximos pasos

- **Tercer color** — si hace falta de verdad, requiere una columna nueva en `tbl_empresas_marca` (coordinar con Leidi).
- **Subida real de logo** — hoy solo se guarda una URL, no hay infraestructura de almacenamiento de archivos.
- **Verificación de dominio propio** — sin DNS ni certificado, es solo un campo de texto por ahora.
- **Sin restricción de rol** — cualquier usuario autenticado de la empresa puede cambiar su marca (no existe todavía el concepto de Rol/Permiso, ver pendiente en README de `usuarios`).

## 9. Cómo compilar/probar localmente

```bash
mvn -f backend/pom.xml install -DskipTests -pl identidad-visual/identidad-visual-domain,identidad-visual/identidad-visual-application,identidad-visual/identidad-visual-infrastructure -am
```

---

## Historial de cambios

- **2026-09-04** — Luis — Documentación inicial: módulo completo (dominio, aplicación, infraestructura), self-service protegido por JWT.
