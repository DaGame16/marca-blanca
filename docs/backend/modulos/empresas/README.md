# Módulo Empresas

> Resumen vivo. Se actualiza *in place* cada vez que el módulo cambia.

## 1. Qué es y qué NO es

**Desde el 2026-09-05, este módulo tiene una sola responsabilidad:** resolver a qué base de datos física pertenece una empresa (multi-tenant: una base Postgres completa por cliente).

**Ya NO le corresponde** administrar el catálogo de módulos activables — eso se extrajo a [`modulos-empresa`](../modulos-empresa/README.md) (ver su [ADR 0001](../modulos-empresa/decisiones/2026-09-05-0001-modulo-separado-de-empresas.md)). Tampoco la marca visual (`identidad-visual`).

## 2. Estructura de paquetes

```
empresas/
├── empresas-domain/.../empresas/domain/
│   ├── Empresa.java, EmpresaConexion.java
│   └── EmpresaSinConexionActivaException.java
├── empresas-application/.../empresas/application/
│   ├── ResolverConexionDeEmpresaService.java, ContextoEmpresaActual.java
│   └── port/
│       ├── in/ResolverConexionDeEmpresa.java
│       └── out/RepositorioEmpresaConexiones.java
└── empresas-infrastructure/.../empresas/infrastructure/
    ├── EmpresaEntity.java, EmpresaConexionEntity.java
    ├── EmpresaConexionJpaRepository.java, RepositorioEmpresaConexionesJpa.java
    └── ConfiguracionEmpresas.java
```

Ya no tiene paquete `web/` — dejó de exponer cualquier endpoint (eso se fue con `modulos-empresa`), y su `pom.xml` ya no depende de `spring-boot-starter-web`.

## 3. Dominio

- **`Empresa`** / **`EmpresaConexion`** — datos de la base de control (`plataforma.tbl_empresas`, `tbl_empresa_conexiones`).
- **`EmpresaSinConexionActivaException`**.

## 4. Aplicación

- **`ResolverConexionDeEmpresa`**: dado un `identificadorEmpresa`, devuelve host/puerto/nombre de base.
- **`ContextoEmpresaActual`**: `ThreadLocal` — guarda qué empresa está activa durante un request. Lo establece `autenticacion` (login/refresh, y en cada request con JWT válido — ver su README), y también internamente cualquier otro módulo que necesite tenant context vía `empresas-application` como dependencia.

## 5. Infraestructura

Sin cambios de fondo desde su versión anterior — `EmpresaEntity`/`EmpresaConexionEntity` mapean el schema `plataforma`, unidad de persistencia "control". El enrutador real (`EnrutadorDataSourcePorEmpresa`) sigue viviendo en `bootstrap`, consumiendo el puerto `ResolverConexionDeEmpresa` de este módulo.

## 6. Contrato REST

**Ninguno.** Este módulo no expone ningún endpoint — la administración de módulos vive en `modulos-empresa`.

## 7. Decisiones de diseño relacionadas

- Ninguna ADR propia de este módulo por ahora. La decisión de separar el catálogo de módulos vive documentada en `modulos-empresa`, no acá.

## 8. Pendiente / próximos pasos

- Crear/dar de baja empresas completas (aprovisionar base de datos nueva) — sin implementar.
- Credenciales de Postgres hardcodeadas en `EnrutadorDataSourcePorEmpresa` (`guajiranet_app`/`guajiranet_app`) — documentado en el propio código como decisión interina para desarrollo, pendiente para infraestructura en QA/PROD.

## 9. Cómo compilar/probar localmente

```bash
mvn -f backend/pom.xml install -DskipTests -pl empresas/empresas-domain,empresas/empresas-application,empresas/empresas-infrastructure -am
```

---

## Historial de cambios

- **2026-09-04** — Luis — Documentación inicial: enrutamiento multi-tenant + módulo de asignación de módulos por empresa.
- **2026-09-05** — Luis — Reducido a solo enrutamiento multi-tenant, tras extraer el catálogo de módulos a `modulos-empresa`.
