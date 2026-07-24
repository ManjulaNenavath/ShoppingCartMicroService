# ---- Build stage ----
# Multi-stage build: compile with Maven, then copy only the jar into a slim JRE.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copy the whole reactor so the parent POM resolves, then build just this module.
COPY . .
RUN mvn -q -pl user-service -am clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/user-service/target/user-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
