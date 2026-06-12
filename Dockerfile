FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM mcr.microsoft.com/playwright/java:v1.52.0-jammy

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends mariadb-client \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/ProyectoFinal-0.0.1-SNAPSHOT.jar app.jar
COPY docker/app-entrypoint.sh /app/docker/app-entrypoint.sh

RUN chmod +x /app/docker/app-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/app/docker/app-entrypoint.sh"]
