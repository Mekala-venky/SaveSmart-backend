# ---- Build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw

# Cache dependencies separately from application sources
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -q -DskipTests package

# ---- Runtime stage ---------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S savesmart && adduser -S savesmart -G savesmart

COPY --from=build /app/target/*.jar app.jar

USER savesmart

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
