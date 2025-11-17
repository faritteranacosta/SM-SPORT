# ---- Build ----
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime ----
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
