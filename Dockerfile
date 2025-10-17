# --- Stage 1: Build the application using Maven ---
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -pl kidcode-web -am

# --- Stage 2: Create the final, smaller runtime image ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/kidcode-web/target/kidcode-web-1.0-SNAPSHOT.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 