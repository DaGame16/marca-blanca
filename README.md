# mi-proyecto

Monorepo con backend Java 25 / Spring Boot 4.1.1 (Maven multi-módulo) y frontend Angular 22.

## Estructura
- `backend/` — API Spring Boot (app-common, app-core, app-security, app-api)
- `frontend/` — SPA Angular + Angular Material
- `infra/` — Docker, Kubernetes, Terraform
- `.github/workflows/` — CI/CD (GitHub Actions → AWS)
- `docs/openapi/` — specs OpenAPI exportadas

## Desarrollo local
```bash
# Backend
cd backend && mvn -pl app-api -am spring-boot:run

# Frontend
cd frontend && npm ci && npm start
```
