# SonarQube local / self-hosted

```bash
cd infra/docker/sonarqube
cp .env.example .env      # cambia SONAR_DB_PASSWORD
docker compose up -d
```

Abre http://localhost:9000, entra con `admin` / `admin` y cambia la contraseña
cuando te lo pida.

## Crear los proyectos

1. **Create Project > Manually**
   - Project key: `mi-proyecto-backend`
   - Project key: `mi-proyecto-frontend`
   (deben coincidir con `sonar.projectKey` en `backend/pom.xml` y
   `frontend/sonar-project.properties`)

## Generar el token para GitHub Actions

`My Account > Security > Generate Token` → tipo *Global Analysis Token* (o
uno por proyecto). Ese valor va en el secret `SONAR_TOKEN` de GitHub.

## Conectar GitHub Actions con este servidor

Los runners de GitHub Actions corren en la nube de GitHub: si SonarQube vive
en tu laptop o en una red privada, el runner **no lo puede alcanzar** por
`http://localhost:9000`. Opciones, de más a menos simple:

- **Self-hosted runner**: instala un runner de GitHub Actions en la misma
  red/máquina donde corre este SonarQube (o en la misma VPC si migras esto a
  un servidor). Es la opción recomendada para uso interno.
- **Exponer el servidor**: si SonarQube vive en un servidor con IP pública
  (o detrás de un dominio con HTTPS, vía Nginx/Caddy + certificado), usa esa
  URL pública como `SONAR_HOST_URL`.
- **SonarCloud**: si no quieres mantener infraestructura propia, es la
  alternativa SaaS sin este problema de red (lo dejamos descartado porque
  pediste self-hosted, pero queda como opción si esto se complica).

En GitHub: `Settings > Secrets and variables > Actions > New repository secret`
- `SONAR_HOST_URL` = la URL desde la que el runner puede llegar al servidor
- `SONAR_TOKEN` = el token generado arriba
