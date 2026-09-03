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

## Docker

```bash
# Backend (puerto 8080)
docker build -t marca-blanca-backend ./backend
docker run -d -p 8080:8080 --name marca-blanca-backend marca-blanca-backend

# Frontend (puerto 4200)
docker build -t marca-blanca-frontend ./frontend
docker run -d -p 4200:80 --name marca-blanca-frontend marca-blanca-frontend
```

Documentación completa (runner self-hosted, SonarQube local, puertos): [`docs/03-docker-y-runner-local.md`](docs/03-docker-y-runner-local.md)
