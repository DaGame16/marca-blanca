# PR — Rediseño empresarial y flujo de creación de empresas

## Título

`feat(frontend): rediseñar plataforma y completar flujo de alta empresarial`

## Resumen

Este cambio reorganiza la experiencia frontend de Marca Blanca para presentar una plataforma empresarial más clara y consistente. Se incorpora un shell autenticado con navegación lateral, se rediseña la landing pública, se mejora el asistente de creación de empresas y se habilita la visualización de subdominios locales derivados del identificador de empresa.

## Alcance funcional

El asistente de creación cubre los siguientes pasos:

1. Captura los datos legales de la empresa y del representante legal.
2. Permite seleccionar módulos.
3. Permite seleccionar visualmente el tema de inicio de sesión.
4. Permite seleccionar visualmente el tema de páginas.
5. Presenta un resumen de configuración y activación gratuita local.
6. Muestra el estado de creación y el subdominio local disponible.

## Cambios principales

### Experiencia autenticada

- Se integró `ShellComponent` como layout de las rutas protegidas.
- Se agregó sidebar con navegación por operación y configuración.
- Se agregó contexto de workspace y estado de cuenta.
- Se agregó perfil, notificaciones y cierre de sesión.
- Se incorporó responsive para pantallas reducidas.

### Landing pública

- Se reemplazó la landing anterior por una presentación empresarial.
- Se agregaron hero, dashboard visual, soluciones, método de implementación y CTA.
- Se conservaron los enlaces a Omnicanal, PBX 3CX, login y registro.

### Registro empresarial

- Se eliminó el campo visible de nombre comercial.
- Se eliminó el campo visible de dominio propio.
- Se agregó el nombre del representante legal.
- Se mantuvieron correo y teléfono del representante.
- Se conserva el identificador como base del subdominio.
- Se agregó botón `Volver al inicio`.
- Se dejó el header fijo durante el desplazamiento.
- Se agregó fallback de módulos cuando la API devuelve vacío o falla.
- Se eliminó el aviso técnico del catálogo de respaldo.

### Temas visuales

- Se ampliaron los previews del diseño de inicio de sesión.
- Se agregaron previews visuales para diseño clásico, compacto y amplio.
- Los previews reflejan colores y logo seleccionados.
- Las tarjetas indican claramente la opción activa.

### Resumen y activación

- Se reemplazó el lenguaje de pago por activación gratuita local.
- Se agregó el botón `Crear empresa`.
- Se muestra el estado de aprovisionamiento.
- Se muestra el subdominio local generado.
- Se agrega acción `Abrir plataforma`.
- El acceso posterior al registro abre el login dentro del subdominio local.

### Compatibilidad de contrato

El backend actual todavía utiliza los campos `nombreComercial` y `dominio` en `RegistrarEmpresaRequest`. Para no romper el endpoint durante la transición, el frontend los envía como `null`, aunque ya no aparecen en la interfaz. La captura de representante, correo y teléfono queda almacenada como información pendiente hasta que el backend exponga dichos campos formalmente.

## Archivos relevantes

| Archivo | Cambio |
|---|---|
| `frontend/src/app/app.routes.ts` | Agrupación de rutas autenticadas bajo el shell. |
| `frontend/src/app/layout/shell.component.ts` | Nuevo layout empresarial. |
| `frontend/src/app/features/home/home.component.ts` | Nueva landing corporativa. |
| `frontend/src/app/features/auth/registro/registro-empresa.component.ts` | Wizard, previews, resumen y subdominio local. |
| `frontend/src/app/features/auth/registro/registro-empresa.models.ts` | Contrato compatible con el backend actual. |
| `frontend/src/app/features/tareas/data/tarea.service.ts` | Corrección a `@Injectable`. |
| `frontend/src/styles.scss` | Tokens y estilos globales. |
| `frontend/angular.json` | Soporte de subdominios localhost. |
| `frontend/tsconfig.json` | Ajuste de helpers TypeScript. |
| `docs/frontend/flujo-creacion-y-rediseño.md` | Documentación técnica completa. |

## Validación

La validación automática quedó condicionada por una instalación incompleta de `node_modules`. El proyecto requiere ejecutar `pnpm install` en el equipo local porque `rxjs` no estaba disponible en el montaje de la sesión.

Comandos de validación:

```powershell
cd C:\xampp\htdocs\mi-proyecto-monorepo\marca-blanca\frontend
pnpm install
pnpm build
pnpm start
```

Pruebas manuales recomendadas:

- Abrir `/registro`.
- Completar los datos de empresa y representante legal.
- Confirmar que el paso de módulos muestra opciones.
- Seleccionar un módulo.
- Verificar los tres previews de login.
- Verificar los tres previews de páginas.
- Crear la empresa.
- Abrir `http://identificador.localhost:4200/`.
- Abrir `http://identificador.localhost:4200/login`.

## Riesgos conocidos

- El contrato backend todavía no recibe formalmente los datos del representante legal.
- El catálogo de respaldo debe retirarse o convertirse en una estrategia controlada cuando la API de producción esté poblada.
- El subdominio `.localhost` es exclusivo de desarrollo y no representa la configuración DNS productiva.
- El aprovisionamiento de base de datos es asíncrono y puede requerir esperar antes del primer login.

## Checklist

- [x] Rediseño visual público.
- [x] Shell autenticado.
- [x] Flujo de creación documentado.
- [x] Campos de representante legal visibles.
- [x] Nombre comercial eliminado de la UI.
- [x] Dominio propio eliminado de la UI.
- [x] Previews visuales para login y páginas.
- [x] Fallback visual de módulos.
- [x] Subdominio local.
- [x] Correcciones TypeScript y Angular.
- [ ] Ejecutar instalación limpia de dependencias.
- [ ] Ejecutar build completo.
- [ ] Validar integración contra backend local.
