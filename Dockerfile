FROM mcr.microsoft.com/playwright/java:v1.52.0-jammy

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]