# Documentación — Plataforma Marca Blanca

Documentación técnica del proyecto, organizada por área. Vive en el repo y se actualiza con cada cambio real — no documenta nada que no esté ya implementado y confirmado.

## Proceso de trabajo
- [`PROCESO-DE-TRABAJO.md`](PROCESO-DE-TRABAJO.md) — reglas de Git del equipo.

## Backend
- [`backend/00-arquitectura-hexagonal.md`](backend/00-arquitectura-hexagonal.md) — punto de partida: estructura, regla de dependencia, convenciones.
- [`backend/modulos/usuarios/`](backend/modulos/usuarios/) — módulo de usuarios (login completo).
- [`backend/modulos/omnicanal/`](backend/modulos/omnicanal/) — módulo de omnicanal (en progreso).

Cada carpeta de módulo tiene su propio `README.md` (qué hay y cómo funciona) y su carpeta `decisiones/` (por qué se decidió así, un archivo por decisión, fechado y sin editar después de creado).

## Frontend
- [`frontend/`](frontend/) — a cargo de Carlos.

## Base de datos
- [`base-de-datos/`](base-de-datos/) — a cargo de Leidi.

## Infraestructura
- [`infraestructura/`](infraestructura/) — Docker, CI/CD, runners — a cargo de Neider.

## Postmortems
- [`postmortems/`](postmortems/) — análisis de incidentes grandes, para no repetirlos.
