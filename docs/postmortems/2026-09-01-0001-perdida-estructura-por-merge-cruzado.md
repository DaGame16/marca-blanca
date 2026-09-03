# Postmortem — Pérdida de la estructura hexagonal por merge cruzado entre main y develop

**Fecha del incidente:** 2026-09-01
**Severidad:** Alta (estructura completa del backend revertida a un scaffold anterior)

## Qué pasó

En algún punto del flujo de PRs, se fusionó `main` hacia `develop` (PR "Merge pull request #6 from desarrolloneider/main"). El problema: `main` nunca había recibido la estructura hexagonal nueva — todo el trabajo siempre se fusionó directo a `develop`, sin promoción a `main`. Al fusionar `main` hacia `develop`, la versión vieja de `main` (el scaffold original `app-api`/`app-common`/`app-core`/`app-security`) pisó la estructura nueva en la punta de `develop`.

Además, en el mismo período se ejecutó un "Revert" sobre un PR ya fusionado (BCrypt/JWT) sin que quedara claro al momento si fue intencional.

## Impacto

`develop` quedó con el scaffold viejo en vez de la estructura hexagonal (`bootstrap`, `usuarios`, `omnicanal`, `shared-kernel`). El commit original con la estructura correcta seguía existiendo en el historial de Git (confirmado con `git merge-base --is-ancestor`), pero ya no era el contenido reflejado en `develop`.

## Causa raíz

Merges bidireccionales entre `main` y `develop` fuera del flujo esperado (`feature/* → develop`, y `develop → main` solo en releases controlados). Fusionar en la dirección contraria reintroduce contenido desactualizado.

## Resolución

Se identificó el último commit bueno conocido (`e99ddc9`), se restauró el contenido de `backend/` desde ese commit sobre una rama nueva (`fix/restaurar-estructura-hexagonal`), se eliminó el scaffold viejo, y se reconstruyeron manualmente las piezas que existían en commits posteriores al restaurado pero no capturadas por ese único commit (AutenticarUsuario, AuthController, SecurityConfig).

## Prevención

- Regla añadida a `docs/PROCESO-DE-TRABAJO.md`: nunca fusionar `main` hacia `develop` ni viceversa fuera del flujo normal.
- Antes de cualquier fusión inusual (releases, hotfixes), confirmar explícitamente la dirección y el contenido esperado antes de confirmar el merge en GitHub.
- Si se usa el botón "Revert" de GitHub sobre un PR ya fusionado, confirmar en el equipo si fue intencional antes de fusionar el PR de reversión que se genera automáticamente.
