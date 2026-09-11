# syntax=docker/dockerfile:1

# ---------- Etapa 1: build ----------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Instala Maven (la imagen de Temurin solo trae el JDK)
RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

# Copiamos primero los pom.xml de todos los módulos para aprovechar la cache de capas de Docker:
# si solo cambia el código fuente, esta capa de dependencias no se vuelve a descargar.
COPY pom.xml ./
COPY shared-kernel/pom.xml shared-kernel/pom.xml
COPY usuarios/usuarios-domain/pom.xml usuarios/usuarios-domain/pom.xml
COPY usuarios/usuarios-application/pom.xml usuarios/usuarios-application/pom.xml
COPY usuarios/usuarios-infrastructure/pom.xml usuarios/usuarios-infrastructure/pom.xml
COPY omnicanal/omnicanal-domain/pom.xml omnicanal/omnicanal-domain/pom.xml
COPY omnicanal/omnicanal-application/pom.xml omnicanal/omnicanal-application/pom.xml
COPY omnicanal/omnicanal-infrastructure/pom.xml omnicanal/omnicanal-infrastructure/pom.xml
COPY bootstrap/pom.xml bootstrap/pom.xml

RUN mvn -B -ntp -q dependency:go-offline || true

# Ahora sí copiamos el código fuente completo y compilamos
COPY . .
RUN mvn -B -ntp clean package -DskipTests

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

# Usuario sin privilegios para no correr como root dentro del contenedor
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=build /app/bootstrap/target/marca-blanca-backend.jar app.jar

EXPOSE 8080

# Forma shell (no exec) para poder expandir variables de entorno antes de
# arrancar. Dos cosas que docker-compose.yml no necesita pero Render si:
#   - PORT: Render lo inyecta y la app debe escuchar ahi (compose no lo
#     define, cae al 8080 de siempre).
#   - DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD: en Render vienen del
#     Postgres administrado (ver render.yaml, fromDatabase) y de ahi se arma
#     el jdbc url -- si ya viene SPRING_DATASOURCE_URL/APROV_MANT_URL/etc
#     seteado explicitamente (como hace docker-compose.yml) eso tiene
#     prioridad y esto no hace nada.
ENTRYPOINT ["sh", "-c", "\
    export SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME}; \
    export SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-$DB_USER}; \
    export SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-$DB_PASSWORD}; \
    export APROV_MANT_URL=${APROV_MANT_URL:-jdbc:postgresql://$DB_HOST:$DB_PORT/postgres}; \
    export APROV_MANT_USER=${APROV_MANT_USER:-$DB_USER}; \
    export APROV_MANT_PASSWORD=${APROV_MANT_PASSWORD:-$DB_PASSWORD}; \
    export APROV_CLIENTE_HOST=${APROV_CLIENTE_HOST:-$DB_HOST}; \
    export APROV_CLIENTE_PUERTO=${APROV_CLIENTE_PUERTO:-$DB_PORT}; \
    exec java -jar app.jar --server.port=${PORT:-8080} \
"]
