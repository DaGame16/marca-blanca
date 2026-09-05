# Módulo Empresas

> Resumen vivo. Se actualiza *in place* cada vez que el módulo cambia.

## 1. Qué es y qué NO es

Dos responsabilidades, relacionadas pero distintas:

1. **Resolver a qué base de datos física pertenece una empresa** (multi-tenant: una base Postgres completa por cliente) — usado por el enrutador de datasource en `bootstrap`.
2. **Administrar qué módulos del producto tiene activos cada empresa** (catálogo + activar/desactivar).

**NO le corresponde (todavía):** crear ni dar de baja empresas completas (eso implica aprovisionar una base de datos nueva — no implementado), ni la marca visual (logo/colores/dominio propio — en diseño).

## 2. Estructura de paquetes

```
empresas/
├── empresas-domain/.../empresas/domain/
│   ├── Empresa.java, EmpresaConexion.java
│   ├── Modulo.java, ModuloDeEmpresa.java
│   └── EmpresaSinConexionActivaException.java, EmpresaNoEncontradaException.java, ModuloNoEncontradoException.java
├── empresas-application/.../empresas/application/
│   ├── ResolverConexionDeEmpresaService.java, ContextoEmpresaActual.java
│   ├── ListarModulosService.java, ListarModulosDeEmpresaService.java
│   ├── ActivarModuloDeEmpresaService.java, DesactivarModuloDeEmpresaService.java
│   └── port/
│       ├── in/ResolverConexionDeEmpresa.java, ListarModulos.java, ListarModulosDeEmpresa.java,
│       │      ActivarModuloDeEmpresa.java, DesactivarModuloDeEmpresa.java
│       └── out/RepositorioEmpresaConexiones.java, RepositorioModulos.java, RepositorioModulosDeEmpresa.java
└── empresas-infrastructure/.../empresas/infrastructure/
    ├── EmpresaEntity.java, EmpresaConexionEntity.java, ModuloEntity.java, EmpresaModuloEntity.java
    ├── (JPA repositories + adaptadores de cada puerto de arriba)
    ├── ConfiguracionEmpresas.java, ConfiguracionModulosDeEmpresa.java
    └── web/
        ├── ModulosAdminController.java
        ├── ClaveAdminInterceptor.java, ClaveAdminInvalidaException.java, ConfiguracionWebAdmin.java
        └── ErrorResponse.java, ManejadorErroresAdmin.java
```

**El enrutador real (`EnrutadorDataSourcePorEmpresa`) NO vive acá — vive en `bootstrap`.** Es infraestructura de arranque (configura una segunda unidad de persistencia de Spring), no un adaptador de un puerto de este módulo. Este módulo solo expone el puerto (`ResolverConexionDeEmpresa`) que el enrutador consume.

## 3. Dominio (`empresas-domain`)

- **`Empresa`** / **`EmpresaConexion`** — datos de la base de control (`plataforma.tbl_empresas`, `tbl_empresa_conexiones`).
- **`Modulo`** — una fila del catálogo de módulos del producto.
- **`ModuloDeEmpresa`** — un módulo + si está activo para una empresa puntual.
- Excepciones: sin conexión activa, empresa no encontrada, módulo no encontrado.

## 4. Aplicación (`empresas-application`)

### Enrutamiento
- **`ResolverConexionDeEmpresa`**: dado un `identificadorEmpresa`, devuelve host/puerto/nombre de base. Lanza `EmpresaSinConexionActivaException` si no hay conexión activa.
- **`ContextoEmpresaActual`**: `ThreadLocal` — guarda qué empresa está activa durante un request. Quien lo establece es responsable de limpiarlo (`finally`), porque el servidor reutiliza threads entre peticiones.

### Módulos por empresa
- **`ListarModulos`**: catálogo completo.
- **`ListarModulosDeEmpresa`**: catálogo + estado activo/inactivo para una empresa.
- **`ActivarModuloDeEmpresa`** / **`DesactivarModuloDeEmpresa`**: idempotentes — activar algo ya activo no falla, solo confirma.

## 5. Infraestructura (`empresas-infrastructure`)

### Persistencia
Todas las entidades (`EmpresaEntity`, `EmpresaConexionEntity`, `ModuloEntity`, `EmpresaModuloEntity`) mapean tablas del schema `plataforma`, en la **base de control** — la unidad de persistencia por defecto de Spring (no la "cliente" que usan `usuarios`/`autenticacion`).

**Nota técnica:** al agregar la segunda unidad de persistencia ("cliente", para `usuarios`/`autenticacion`), Spring Boot deja de auto-configurar la unidad por defecto — por eso `bootstrap/ConfiguracionPersistenciaControl` la declara a mano (`@EnableJpaRepositories` + `DataSource` marcado `@Primary`). Sin eso, ninguna consulta de este módulo funcionaría.

### `web/` — protección por clave compartida (interina)

No existe todavía un sistema de identidad de "administrador de plataforma" — el único JWT que existe identifica a un usuario *dentro* de una empresa. Mientras tanto, `/api/v1/admin/**` se protege con un header `X-Admin-Key` comparado contra `app.admin.clave` — mismo patrón que usa Odoo para sus operaciones de gestión de bases de datos (`admin_passwd`).

Implementado con un `HandlerInterceptor` (`ClaveAdminInterceptor`), no un `Filter` — corre después de que la cadena de Spring Security ya dejó pasar el request (esas rutas están en `permitAll()`), evitando problemas de orden de inicialización entre el filtro y el interceptor.

**Esto es una decisión consciente, no definitiva** — el día que haya más de una persona con necesidad de acceso diferenciado (auditoría por persona, permisos distintos entre sí), corresponde reemplazarlo por un sistema de identidad real.

## 6. Contrato REST

| Método | Ruta | Protección | Respuesta OK | Errores |
|---|---|---|---|---|
| GET | `/api/v1/admin/modulos` | `X-Admin-Key` | 200 + lista de `Modulo` | 401 |
| GET | `/api/v1/admin/empresas/{id}/modulos` | `X-Admin-Key` | 200 + lista de `ModuloDeEmpresa` | 401 · 404 |
| POST | `/api/v1/admin/empresas/{id}/modulos/{codigo}/activar` | `X-Admin-Key` | 204 | 401 · 404 |
| POST | `/api/v1/admin/empresas/{id}/modulos/{codigo}/desactivar` | `X-Admin-Key` | 204 | 401 · 404 |

## 7. Decisiones de diseño relacionadas

*Pendiente* — este módulo todavía no tiene ninguna ADR propia. Dos decisiones tomadas en el camino ameritan una: (1) por qué se extendió `empresas` en vez de crear un módulo nuevo para módulos-por-empresa, (2) la clave compartida como protección interina. Falta escribirlas.

## 8. Pendiente / próximos pasos

- **Escribir las ADR pendientes** (ver sección 7).
- **Crear/dar de baja empresas completas** — hoy solo se administra el estado de módulos de una empresa que ya existe.
- **Marca blanca visual** (logo, colores, dominio propio) — `tbl_empresas_marca` ya existe en la base, sin código todavía.
- **Reemplazar la clave compartida** por un sistema real de identidad de administrador de plataforma, cuando haga falta.
- **Datos de prueba** — no hay ninguna empresa cargada en ningún ambiente de desarrollo todavía; bloquea probar el login y este módulo de punta a punta.

## 9. Cómo compilar/probar localmente

```bash
mvn -f backend/pom.xml install -DskipTests -pl empresas/empresas-domain,empresas/empresas-application,empresas/empresas-infrastructure -am
```

---

## Historial de cambios

- **2026-09-04** — Luis — Documentación inicial: enrutamiento multi-tenant + módulo de asignación de módulos por empresa.
