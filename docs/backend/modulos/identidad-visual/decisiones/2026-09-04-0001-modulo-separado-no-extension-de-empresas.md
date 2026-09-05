# ADR 0001 — Módulo separado, no extensión de `empresas`

**Fecha:** 2026-09-04
**Estado:** Aceptada

## Resumen

La marca blanca visual (logo, colores, dominio propio) vive en su propio módulo, `identidad-visual` — no se agregó al módulo `empresas`, a pesar de que su tabla (`tbl_empresas_marca`) vive en la misma base de control que `tbl_empresas`.

## Contexto

`empresas` ya cargaba dos responsabilidades (resolver conexión multi-tenant, administrar módulos activos por empresa) cuando surgió la necesidad de agregar la marca visual. Ambas responsabilidades anteriores son controladas por la **plataforma** (protegidas por clave de administrador) — la marca visual, en cambio, la configura **el propio cliente** sobre sí mismo, vía su JWT normal. Es un patrón de acceso completamente distinto, no solo un dato más.

## Opciones evaluadas

| Opción | Consideración |
|---|---|
| Extender `empresas` (mismo criterio usado para el catálogo de módulos) | Mezclaría en un mismo módulo dos modelos de seguridad opuestos: admin-de-plataforma vs. self-service-del-cliente. Un cajón de sastre a mediano plazo. |
| **Módulo nuevo, `identidad-visual`** (elegida) | Separa por quién tiene permiso de tocar qué, no solo por en qué base vive el dato. |

## Decisión

Módulo nuevo con estructura hexagonal completa (domain/application/infrastructure), sin depender de `empresas` en código — tiene su propio mapeo mínimo y de solo lectura de `tbl_empresas` (`EmpresaRefEntity`, solo id/uuid/identificador/estado) para resolver el id interno de la empresa, igual que `autenticacion` no depende de `usuarios-infrastructure` para consultar `tbl_sesiones`.

La empresa se identifica por su `identificador` (slug), no por su UUID — es el único dato que existe de verdad en toda la cadena de autenticación (login, JWT, `ContextoEmpresaActual`); introducir el UUID acá hubiera significado una conversión que no existe en ningún otro punto del sistema.

## Consecuencias

| Capa | Impacto |
|---|---|
| Dominio | `ColorHex` (value object, valida formato hexadecimal), `MarcaDeEmpresa`, `EmpresaNoEncontradaException` — todo nuevo, sin relación de código con `empresas-domain`. |
| Aplicación | 2 casos de uso (`ObtenerMarcaDeEmpresa`, `ActualizarMarcaDeEmpresa`), ninguna dependencia de otro módulo. |
| Infraestructura | Nueva entidad `EmpresaMarcaEntity` + `EmpresaRefEntity` (lectura mínima). Se sumó al `@EntityScan`/`@EnableJpaRepositories` de la unidad de persistencia "control", junto a `empresas.infrastructure`. |
| Pendiente | Por ahora 2 colores (`primario`/`secundario`); un tercero requiere que Leidi agregue la columna en la tabla real primero. No hay subida de archivos (el logo se guarda como URL). El dominio propio se guarda sin verificación DNS/certificado. |

## Cómo se podría revertir o evolucionar

Si algún día "empresas" y "identidad-visual" necesitaran compartir lógica real (no solo la tabla de control), se podría introducir un módulo `plataforma` común del que ambos dependan — pero hoy no hay ninguna necesidad concreta de eso, solo la tabla vive cerca.
