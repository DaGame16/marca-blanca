# ADR 0001 — Identificador de empresa explícito en el login

**Fecha:** 2026-09-03
**Estado:** Aceptada

## Resumen

El login pide el identificador de la empresa junto con correo y contraseña, en vez de resolverlo automáticamente por subdominio.

## Contexto

Cada empresa cliente tiene su propia base de datos, físicamente separada. Esto significa que un mismo correo puede existir en más de una empresa a la vez sin ningún conflicto — por ejemplo, `ana@empresa.com` podría ser una cuenta válida tanto en la base de `EmpresaA` como en la de `EmpresaB`. El sistema **no tiene forma de saber a cuál base consultar** sin un dato adicional que identifique la empresa antes de buscar el correo.

## Opciones evaluadas

| Opción | Cómo funciona | Ventajas | Desventajas |
|---|---|---|---|
| Subdominio (`empresa.tudominio.com`) | El subdominio resuelve el tenant antes de llegar al formulario de login | Experiencia más limpia, el usuario no escribe nada extra | Requiere DNS con subdominios comodín, certificado SSL por subdominio (o wildcard), configuración adicional de proxy/ingress |
| **Identificador explícito** (elegida) | El formulario de login pide un campo adicional | Cero infraestructura nueva necesaria; funciona igual en local, DEV, QA y PROD sin configurar nada | Un campo más que la persona tiene que completar |

## Decisión

Identificador explícito en el formulario de login.

## Ejemplo

```json
POST /api/v1/auth/login
{
  "correo": "ana@empresa.com",
  "contrasena": "********",
  "identificadorEmpresa": "empresa-abc"
}
```

## Consecuencias

| Capa | Impacto |
|---|---|
| Dominio | Ninguno — `Usuario` no sabe nada de empresas ni de multi-tenancy. |
| Aplicación | Ninguno — `AutenticarUsuarioService` recibe correo y contraseña, nada más. |
| Infraestructura | `LoginRequest` ya incluye `identificadorEmpresa`, pero **el enrutamiento real** (elegir qué `DataSource` usar según ese valor) todavía no está implementado. |
| Pendiente | Definir el mecanismo real de selección de base de datos por empresa (ver `README.md` del módulo, sección 8). |

## Cómo se podría revertir o evolucionar

Si más adelante el negocio pide la experiencia de subdominio, se resuelve agregando un filtro en infraestructura que extraiga el identificador del header `Host` en vez de leerlo del body — el contrato interno (`Correo`, `Contrasena`, identificador de empresa) no cambia, ni tampoco dominio ni aplicación.
