FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM mcr.microsoft.com/playwright/java:v1.52.0-jammy

WORKDIR /app

COPY --from=build /app/target/ProyectoFinal-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
